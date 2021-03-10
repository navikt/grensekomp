package no.nav.helse.slowtests

import com.zaxxer.hikari.HikariDataSource
import no.nav.helse.GravidTestData
import no.nav.helse.grensekomp.db.PostgresGravidSoeknadRepository
import no.nav.helse.grensekomp.db.createTestHikariConfig
import no.nav.helse.slowtests.systemtests.api.SystemTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.get
import kotlin.test.assertNotNull

class PostgresGravidSoeknadRepositoryTest : SystemTestBase() {

    lateinit var repo: PostgresGravidSoeknadRepository
    val testSoeknad = GravidTestData.soeknadGravid

    @BeforeEach
    internal fun setUp() {
        val ds = HikariDataSource(createTestHikariConfig())

        repo = PostgresGravidSoeknadRepository(ds, get())
        repo.insert(testSoeknad)

    }

    @AfterEach
    internal fun tearDown() {
       repo.delete(testSoeknad.id)
    }

    @Test
    fun `finnerDataIDb`() {
        val soeknadGravidResult = repo.getById(testSoeknad.id)
        assertThat(soeknadGravidResult).isEqualTo(testSoeknad)
    }

    @Test
    fun `kanOppdatereData`() {
        val soeknadGravidResult = repo.getById(testSoeknad.id)
        assertNotNull(soeknadGravidResult, "Må finnes")

        soeknadGravidResult.journalpostId = "1234"
        soeknadGravidResult.oppgaveId = "78990"

        repo.update(soeknadGravidResult)

        val afterUpdate = repo.getById(soeknadGravidResult.id)
        assertThat(afterUpdate).isEqualTo(soeknadGravidResult)
    }

}
