package no.nav.helse.slowtests.systemtests.api

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.db.PostgresRefusjonskravRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.get

class RefusjonskravHTTPTests : SystemTestBase() {
    private val refusjonsKravUrl = "/api/v1/refusjonskrav"
    lateinit var repo: PostgresRefusjonskravRepository
    val testKrav = TestData.gyldigKrav

    @BeforeEach
    internal fun setUp() {
        val ds = HikariDataSource(createTestHikariConfig())
        repo = PostgresRefusjonskravRepository(ds, get())
        repo.insert(testKrav)
    }

    @AfterEach
    internal fun tearDown() {
        repo.delete(testKrav.id)
    }

    @Test
    fun `Skal returnere krav list`() = suspendableTest {
        val response = httpClient.get<HttpResponse> {
            appUrl("$refusjonsKravUrl/list/${TestData.validOrgNr}")
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
        }
        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.content).isNotNull
    }

    @Test
    fun `Skal returnere oppdatert status på krav til slettet`() = suspendableTest {

        val response = httpClient.delete<HttpResponse> {
            appUrl("$refusjonsKravUrl/${testKrav.id}")
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
        }
        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.content).isNotNull
    }


    /*
    1- lagre a soknad
    2- prøve å lagre en soknad med sammen person og period ==> feil
    3- prøve å lagre en soknad med sammen person og en overløpende period ==> feil
    3- prove å lagre en søknad med sammen person med en ny period ==> OK
    * */
    @Test
    @Disabled
    fun `søknader må ikke ha overløpende perioder for sammen person`()= suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl("$refusjonsKravUrl/list")
            contentType(ContentType.Application.Json)
            loggedInAs("123456789")
            body = TestData.gyldigKrav
        }
        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }
}
