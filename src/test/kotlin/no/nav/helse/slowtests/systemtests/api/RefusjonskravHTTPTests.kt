package no.nav.helse.slowtests.systemtests.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.db.PostgresRefusjonskravRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.web.api.dto.PostListResponseDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.get
import java.time.LocalDate
import kotlin.text.Charsets

class RefusjonskravHTTPTests : SystemTestBase() {
    private val refusjonsKravUrl = "/api/v1/refusjonskrav"
    lateinit var repo: PostgresRefusjonskravRepository
    val testKrav = TestData.gyldigKrav
    val objectMapper = jacksonObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)


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


    @Test
    fun `Skal retunere OverloependePerioderConstraints hvis periodene overlapper hverandre for samme person`()= suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl("$refusjonsKravUrl/list")
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            (Charsets.UTF_8)

            loggedInAs("123456789")
            body=  listOf(TestData.gyldigSoeknad)
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val errs = objectMapper.readValue(response.call.response.receive<ByteArray>(),
            object: TypeReference<List<PostListResponseDto>>() {})
        Assertions.assertThat(errs.size).isGreaterThan(0)
        Assertions.assertThat(errs.first().status).isEqualTo(PostListResponseDto.Status.VALIDATION_ERRORS)
        Assertions.assertThat(errs.first().validationErrors?.get(0)?.validationType).isEqualTo("OverloependePerioderConstraints")

    }

    @Test
    fun `Skal ikke retunere OverloependePerioderConstraints hvis periodene ikke overlapper hverandre for samme person`()= suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl("$refusjonsKravUrl/list")
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            loggedInAs("123456789")
            val dtocopy = TestData.gyldigSoeknad.copy(periode =
            Periode(
                LocalDate.of(2021, 2, 6),
                LocalDate.of(2021, 2, 10),
                1000)
            )
            body= listOf(dtocopy)

        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val errs = objectMapper.readValue(response.call.response.receive<ByteArray>(),
            object: TypeReference<List<PostListResponseDto>>() {})
        Assertions.assertThat(errs.size).isGreaterThan(0)
        Assertions.assertThat(errs.first().status).isEqualTo(PostListResponseDto.Status.OK)
    }

    @Test
    fun `Skal retunere OverloependePerioderConstraints hvis samme periode brukes for samme person`()= suspendableTest {
        val response = httpClient.post<HttpResponse> {
            appUrl("$refusjonsKravUrl/list")
            contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            loggedInAs("123456789")
            body= listOf(TestData.gyldigSoeknad.copy(periode =
            Periode(
                LocalDate.of(2021, 1, 30),
                LocalDate.of(2021, 2, 5),
                1000)
            ))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val errs = objectMapper.readValue(response.call.response.receive<ByteArray>(),
            object: TypeReference<List<PostListResponseDto>>() {})
        Assertions.assertThat(errs.size).isGreaterThan(0)
        Assertions.assertThat(errs.first().status).isEqualTo(PostListResponseDto.Status.VALIDATION_ERRORS)
        Assertions.assertThat(errs.first().validationErrors?.get(0)?.validationType).isEqualTo("OverloependePerioderConstraints")

    }
}
