package no.nav.helse.grensekomp.web.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.*
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.arbeidsgiver.web.auth.AltinnAuthorizer
import no.nav.helse.grensekomp.web.auth.hentIdentitetsnummerFraLoginToken
import no.nav.helse.grensekomp.web.auth.hentUtløpsdatoFraLoginToken
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.metrics.INNKOMMENDE_REFUSJONSKRAV_BELOEP_COUNTER
import no.nav.helse.grensekomp.metrics.INNKOMMENDE_REFUSJONSKRAV_COUNTER
import no.nav.helse.grensekomp.service.RefusjonskravService
import no.nav.helse.grensekomp.web.api.dto.PostListResponseDto
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import no.nav.helse.grensekomp.web.api.dto.validation.ValidationProblemDetail
import no.nav.helse.grensekomp.web.api.dto.validation.getContextualMessage
import no.nav.helse.grensekomp.web.api.dto.validation.validerArbeidsforhold
import org.koin.ktor.ext.get
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.ForbiddenException
import kotlin.collections.ArrayList

private val excelContentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
val logger = LoggerFactory.getLogger("grensekompRoutes")

@KtorExperimentalAPI
fun Route.grensekompRoutes(
    authorizer: AltinnAuthorizer,
    refusjonskravService: RefusjonskravService,
    aaregClient: AaregArbeidsforholdClient
) {
    route("/login-expiry") {
        get {
            call.respond(HttpStatusCode.OK, hentUtløpsdatoFraLoginToken(application.environment.config, call.request))
        }
    }

    route("/refusjonskrav") {
        get("/list/{virksomhetsnummer}") {
            val innloggetFnr = hentIdentitetsnummerFraLoginToken(application.environment.config, call.request)
            val om = application.get<ObjectMapper>()
            val virksomhetnr = call.parameters["virksomhetsnummer"]

            if(virksomhetnr == null)
                call.respond(HttpStatusCode.NotAcceptable, "Må ha virksomhetsnummer")
            else {
                val refusjonkravliste = refusjonskravService.getAllForVirksomhet(virksomhetnr)
                call.respond(HttpStatusCode.OK, om.writeValueAsString(refusjonkravliste))
            }
        }
        post("/list") {
            val refusjonskravJson = call.receiveText()
            val om = application.get<ObjectMapper>()
            val jsonTree = om.readTree(refusjonskravJson)
            val responseBody = ArrayList<PostListResponseDto>(jsonTree.size())
            val domeneListeMedIndex = mutableMapOf<Int, Refusjonskrav>()
            val opprettetAv = hentIdentitetsnummerFraLoginToken(
                application.environment.config,
                call.request
            )
            val innsendingstidspunkt = LocalDateTime.now()

            for (i in 0 until jsonTree.size())
                responseBody.add(i, PostListResponseDto(PostListResponseDto.Status.GENERIC_ERROR))

            for (i in 0 until jsonTree.size()) {
                try {
                    val dto = om.readValue<RefusjonskravDto>(jsonTree[i].traverse())
                    authorize(authorizer, dto.virksomhetsnummer)
                    validerArbeidsforhold(aaregClient, dto)

                    domeneListeMedIndex[i] = Refusjonskrav(
                        opprettetAv,
                        dto.identitetsnummer,
                        dto.virksomhetsnummer,
                        dto.periode,
                        dto.bekreftet,
                        dto.bostedsland,
                        opprettet = innsendingstidspunkt
                    )
                } catch (forbiddenEx: ForbiddenException) {
                    responseBody[i] = PostListResponseDto(
                        status = PostListResponseDto.Status.GENERIC_ERROR,
                        genericMessage = "Ingen tilgang til virksomheten"
                    )
                } catch (validationEx: ConstraintViolationException) {
                    val problems = validationEx.constraintViolations.map {
                        ValidationProblemDetail(it.constraint.name, it.getContextualMessage(), it.property, it.value)
                    }
                    responseBody[i] = PostListResponseDto(
                        status = PostListResponseDto.Status.VALIDATION_ERRORS,
                        validationErrors = problems
                    )
                } catch (genericEx: Exception) {
                    if (genericEx.cause is ConstraintViolationException) {
                        val problems = (genericEx.cause as ConstraintViolationException).constraintViolations.map {
                            ValidationProblemDetail(
                                it.constraint.name,
                                it.getContextualMessage(),
                                it.property,
                                it.value
                            )
                        }
                        responseBody[i] = PostListResponseDto(
                            status = PostListResponseDto.Status.VALIDATION_ERRORS,
                            validationErrors = problems
                        )
                    } else {
                        logger.error("Feil under prosessering", genericEx)
                        responseBody[i] = PostListResponseDto(
                            status = PostListResponseDto.Status.GENERIC_ERROR,
                            genericMessage = genericEx.message
                        )
                    }
                }
            }
            if (domeneListeMedIndex.isNotEmpty()) {
                val savedList = refusjonskravService.saveKravListWithKvittering(domeneListeMedIndex)
                savedList.forEach { item ->
                    INNKOMMENDE_REFUSJONSKRAV_COUNTER.inc()
                    INNKOMMENDE_REFUSJONSKRAV_BELOEP_COUNTER.inc(item.value.periode.dagsats.div(1000))
                    responseBody[item.key] = PostListResponseDto(status = PostListResponseDto.Status.OK)
                }
            }
            call.respond(HttpStatusCode.OK, responseBody)
        }
    }

    route("/bulk") {

        get("/template") {
            val template = javaClass.getResourceAsStream("/bulk-upload/inntektskompensasjon_mal_v10-03-2021.xlsx")
            call.response.headers.append("Content-Disposition", "attachment; filename=\"inntektskompensasjon.xlsx\"")
            call.respondBytes(template.readAllBytes(), excelContentType)
        }

        post("/upload") {
            throw ForbiddenException("Denne funksjonen er ikke implementert")

            /*val id = hentIdentitetsnummerFraLoginToken(application.environment.config, call.request)
            val multipart = call.receiveMultipart()

            val fileItem = multipart.readAllParts()
                .filterIsInstance<PartData.FileItem>()
                .firstOrNull()
                ?: throw IllegalArgumentException()

            val maxUploadSize = 250 * 1024

            val bytes = fileItem.streamProvider().readNBytes(maxUploadSize + 1)

            if (bytes.size > maxUploadSize) {
                throw IOException("Den opplastede filen er for stor")
            }

            ExcelBulkService(refusjonskravService, ExcelParser(authorizer))
                .processExcelFile(bytes.inputStream(), id)

            call.respond(HttpStatusCode.OK, "Søknaden er mottatt.")*/
        }
    }
}

@KtorExperimentalAPI
private fun PipelineContext<Unit, ApplicationCall>.authorize(authorizer: AltinnAuthorizer, arbeidsgiverId: String) {
    val identitetsnummer = hentIdentitetsnummerFraLoginToken(application.environment.config, call.request)
    if (!authorizer.hasAccess(identitetsnummer, arbeidsgiverId)) {
        throw ForbiddenException()
    }
}
