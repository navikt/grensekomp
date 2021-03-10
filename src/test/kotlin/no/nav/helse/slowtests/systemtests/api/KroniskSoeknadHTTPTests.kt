package no.nav.helse.slowtests.systemtests.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.GravidTestData
import no.nav.helse.KroniskTestData
import no.nav.helse.fritakagp.db.KroniskSoeknadRepository
import no.nav.helse.fritakagp.domain.*
import no.nav.helse.fritakagp.web.api.resreq.KroniskSoknadRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.test.inject

class KroniskSoeknadHTTPTests : SystemTestBase() {
    private val soeknadKroniskUrl = "/api/v1/kronisk/soeknad"

    @Test
    internal fun `Returnerer søknaden når korrekt bruker er innlogget, 404 når ikke`() = suspendableTest {
        val repo by inject<KroniskSoeknadRepository>()

        repo.insert(KroniskTestData.soeknadKronisk)

        val accessDenied = httpClient.get<HttpResponse> {
            appUrl("$soeknadKroniskUrl/${KroniskTestData.soeknadKronisk.id}")
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
        }

        Assertions.assertThat(accessDenied.status).isEqualTo(HttpStatusCode.NotFound)

        val accessGrantedForm = httpClient.get<KroniskSoeknad> {
            appUrl("$soeknadKroniskUrl/${KroniskTestData.soeknadKronisk.id}")
            contentType(ContentType.Application.Json)
            loggedInAs(KroniskTestData.soeknadKronisk.identitetsnummer)
        }

        Assertions.assertThat(accessGrantedForm).isEqualTo(KroniskTestData.soeknadKronisk)
    }


    @Test
    fun `invalid enum fields gives 400 Bad request`() = suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl(soeknadKroniskUrl)
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")

            body = """
                {
                    "fnr": "${GravidTestData.validIdentitetsnummer}",
                    "orgnr": "${GravidTestData.fullValidSoeknadRequest.virksomhetsnummer}",
                    "bekreftelse": true,
                    "paakjenningstyper": ["IKKE GYLDIG"]
                }
            """.trimIndent()
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `Skal returnere Created ved feilfritt skjema uten fil`() = suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl(soeknadKroniskUrl)
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
            body = KroniskTestData.fullValidRequest
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Created)
    }


    @Test
    fun `Skal validere feil ved ugyldig data`() = suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl(soeknadKroniskUrl)
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
            body = KroniskSoknadRequest(virksomhetsnummer = "lkajsbdfv",
                identitetsnummer = "lkdf",
                paakjenningBeskrivelse = "sdfsfd",
                arbeidstyper = setOf(ArbeidsType.KREVENDE),
                paakjenningstyper = setOf(PaakjenningsType.ALLERGENER),
                fravaer = setOf(FravaerData("2001-01",12)),
                bekreftet = true,
                dokumentasjon = null
            )
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
    }

    @Test
    fun `Skal returnere Created når fil er vedlagt`() = suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl(soeknadKroniskUrl)
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
            body = KroniskTestData.kroniskSoknadMedFil
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Created)
    }
}