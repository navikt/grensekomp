package no.nav.helse.slowtests.systemtests.api

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.db.PostgresRefusjonskravRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import no.nav.helse.grensekomp.web.api.dto.PostListResponseDto
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.get

class ValidationMessagesHTTPTests : SystemTestBase() {
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
        repo.deleteAll()
    }

    @Test
    fun `Skal returnere feilmeldinger på korrekt språk`()= suspendableTest {
        val response = httpClient.post<List<PostListResponseDto>> {
            appUrl("$refusjonsKravUrl/list")
            header(HttpHeaders.AcceptLanguage, "en")
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            loggedInAs("123456789")
            body=  listOf(TestData.gyldigSoeknad.copy(bekreftet = false))
        }

        val first = response.first()

        Assertions.assertThat(first.status).isEqualTo(PostListResponseDto.Status.VALIDATION_ERRORS)
        val message = first.validationErrors?.find { it.propertyPath == RefusjonskravDto::bekreftet.name }?.message
        Assertions.assertThat(message).contains("attest")
    }
}
