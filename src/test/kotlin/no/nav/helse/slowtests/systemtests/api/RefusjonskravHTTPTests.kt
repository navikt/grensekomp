package no.nav.helse.slowtests.systemtests.api

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.db.PostgresRefusjonskravRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import no.nav.helse.grensekomp.domene.Refusjonskrav
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

}
