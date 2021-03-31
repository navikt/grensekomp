package no.nav.helse.slowtests.systemtests.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.grensekomp.TestData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class RefusjonskravHTTPTests : SystemTestBase() {
    private val refusjonsKravUrl = "/api/v1/refusjonskrav"

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
