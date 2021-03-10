package no.nav.helse.slowtests

import com.zaxxer.hikari.HikariDataSource
import no.nav.helse.GravidTestData
import no.nav.helse.grensekomp.db.PostgresGravidKravRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import no.nav.helse.slowtests.systemtests.api.SystemTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.get
import kotlin.test.assertNotNull

class PostgresGravidKravRepositoryTest : SystemTestBase() {

    lateinit var repo: PostgresGravidKravRepository
    val testKrav = GravidTestData.gravidKrav

    @BeforeEach
    internal fun setUp() {
        val ds = HikariDataSource(createTestHikariConfig())

        repo = PostgresGravidKravRepository(ds, get())
        repo.insert(testKrav)

    }

    @AfterEach
    internal fun tearDown() {
       repo.delete(testKrav.id)
    }

    @Test
    fun `test getById`() {
        val soeknadKroniskResult = repo.getById(testKrav.id)
        assertThat(soeknadKroniskResult).isEqualTo(testKrav)
    }

    @Test
    fun `kan oppdatere data`() {
        val soeknadKroniskResult = repo.getById(testKrav.id)
        assertNotNull(soeknadKroniskResult, "Må finnes")

        soeknadKroniskResult.journalpostId = "1234"
        soeknadKroniskResult.oppgaveId = "78990"

        repo.update(soeknadKroniskResult)

        val afterUpdate = repo.getById(soeknadKroniskResult.id)
        assertThat(afterUpdate).isEqualTo(soeknadKroniskResult)
    }

}
