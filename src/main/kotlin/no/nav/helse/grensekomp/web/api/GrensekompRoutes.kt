package no.nav.helse.grensekomp.web.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.get
import io.ktor.util.*
import io.ktor.util.pipeline.*
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClientImpl
import no.nav.helse.arbeidsgiver.system.getString
import no.nav.helse.arbeidsgiver.web.auth.AltinnAuthorizer
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.metrics.PDL_VALIDERINGER
import no.nav.helse.grensekomp.service.RefusjonskravService
import no.nav.helse.grensekomp.web.api.dto.PostListResponseDto
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import no.nav.helse.grensekomp.web.api.dto.validation.*
import no.nav.helse.grensekomp.web.auth.hentIdentitetsnummerFraLoginToken
import no.nav.helse.grensekomp.web.dto.validation.BostedlandValidator.Companion.tabeller.godkjenteBostedsKoder
import org.koin.ktor.ext.get
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.any
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set

@KtorExperimentalAPI
fun Route.grensekompRoutes(
    authorizer: AltinnAuthorizer,
    refusjonskravService: RefusjonskravService,
    aaregClient: AaregArbeidsforholdClient,
    pdlClient: PdlClient
) {

    route("/refusjonskrav") {
        get("/list/{virksomhetsnummer}") {
            val virksomhetnr = call.parameters["virksomhetsnummer"]

            if(virksomhetnr == null) {
                call.respond(HttpStatusCode.NotAcceptable, "Må ha virksomhetsnummer")
            } else {
                authorize(authorizer, virksomhetnr)
                val refusjonkravliste = refusjonskravService.getAllForVirksomhet(virksomhetnr)
                call.respond(HttpStatusCode.OK, refusjonkravliste)
            }
        }

        delete("/{id}"){
            val reufusjonskravId = UUID.fromString(call.parameters["id"])
            var refusjonskrav = refusjonskravService.getKrav(reufusjonskravId)
            val om = application.get<ObjectMapper>()
            if(refusjonskrav == null)
                call.respond(HttpStatusCode.NotFound, "Fant ikke refusjonskrav med id $reufusjonskravId")
            else {
                val virksomhetnr = refusjonskrav.virksomhetsnummer
                authorize(authorizer, virksomhetnr)
                refusjonskrav = refusjonskravService.cancelKrav(refusjonskrav.id)
                call.respond(HttpStatusCode.OK, om.writeValueAsString(refusjonskrav))
            }
        }

        post("/list") {
            val logger = LoggerFactory.getLogger("API")
            val locale = if (call.request.headers[HttpHeaders.AcceptLanguage] == "en") Locale.ENGLISH else Locale.forLanguageTag("no-NB")

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

            val fristMnd = application.environment.config.getString("soekefrist_i_mnd")

            for (i in 0 until jsonTree.size())
                responseBody.add(i, PostListResponseDto(PostListResponseDto.Status.OK))

            for (i in 0 until jsonTree.size()) {
                try {
                    val dto = om.readValue<RefusjonskravDto>(jsonTree[i].traverse())
                    dto.validate(fristMnd.toLong())
                    authorize(authorizer, dto.virksomhetsnummer)
                    val personData = pdlClient.fullPerson(dto.identitetsnummer)

                    val aktuelleArbeidsforhold = aaregClient.hentArbeidsforhold(dto.identitetsnummer, UUID.randomUUID().toString())
                        .filter { it.arbeidsgiver.organisasjonsnummer == dto.virksomhetsnummer }

                    validerPdlBaserteRegler(personData, dto)
                    validerArbeidsforhold(aktuelleArbeidsforhold, dto,)
                    validerKravPerioden(dto, refusjonskravService)

                    val erEØSBorger = personData?.hentPerson?.statsborgerskap?.any { s -> godkjenteBostedsKoder.contains(s.land) } ?: false
                    val erDød = personData?.hentPerson?.trekkUtDoedsfalldato() != null
                    val etteranmeldtArbeidsforhold = aktuelleArbeidsforhold.any { it?.registrert?.toLocalDate()?.isAfter(Periode.refusjonFraDato) ?: false }

                    domeneListeMedIndex[i] = Refusjonskrav(
                        opprettetAv,
                        dto.identitetsnummer,
                        dto.virksomhetsnummer,
                        dto.periode,
                        dto.bekreftet,
                        dto.bostedsland,
                        etteranmeldtArbeidsforhold = etteranmeldtArbeidsforhold,
                        erDød = erDød,
                        erEØSStatsborger = erEØSBorger,
                        opprettet = innsendingstidspunkt
                    )
                } catch (forbiddenEx: ForbiddenException) {
                    logger.error(forbiddenEx)
                    responseBody[i] = PostListResponseDto(
                        status = PostListResponseDto.Status.GENERIC_ERROR,
                        genericMessage = "Ingen tilgang til virksomheten"
                    )
                }catch (pdlError: PdlClientImpl.PdlException) {
                    logger.error(pdlError)
                    PDL_VALIDERINGER.labels("finnes_ikke").inc()
                    responseBody[i] = PostListResponseDto(
                        status = PostListResponseDto.Status.VALIDATION_ERRORS,
                        validationErrors = listOf(ValidationProblemDetail(
                    "PDL",
                    "Feil ved henting av personinformasjon",
                                RefusjonskravDto::identitetsnummer.name,
                                invalidValue = null
                            )
                        )
                    )

                } catch (validationEx: ConstraintViolationException) {
                    val problems = validationEx.constraintViolations.map {
                        ValidationProblemDetail(it.constraint.name, it.getContextualMessage(locale), it.property, it.value)
                    }
                    responseBody[i] = PostListResponseDto(
                        status = PostListResponseDto.Status.VALIDATION_ERRORS,
                        validationErrors = problems
                    )
                } catch (genericEx: Exception) {
                    logger.info("Feil under prosessering $genericEx")
                    logger.error("Feil under prosessering", genericEx)

                    if (genericEx.cause is ConstraintViolationException) {
                        val problems = (genericEx.cause as ConstraintViolationException).constraintViolations.map {
                            ValidationProblemDetail(
                                it.constraint.name,
                                it.getContextualMessage(locale),
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

            val hasErrors = responseBody.any { it.status != PostListResponseDto.Status.OK }

            if (hasErrors) {
                responseBody.filter { it.status == PostListResponseDto.Status.OK }.forEach { it.status = PostListResponseDto.Status.VALIDATION_ERRORS }
            } else {
                if (domeneListeMedIndex.isNotEmpty()) {
                    val savedList = refusjonskravService.saveKravListWithKvittering(domeneListeMedIndex)
                    savedList.forEach { item ->
                        responseBody[item.key] = PostListResponseDto(status = PostListResponseDto.Status.OK)
                    }
                }
            }

            call.respond(HttpStatusCode.OK, responseBody)
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
