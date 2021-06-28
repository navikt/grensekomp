package no.nav.helse.slowtests

import no.nav.helse.grensekomp.datapakke.DatapakkePublisherJob
import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.integration.GrunnbeløpClient
import no.nav.helse.slowtests.systemtests.api.SystemTestBase
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.test.inject

class DatapakkePublisherJobTest : SystemTestBase() {

    val repo by inject<RefusjonskravRepository>()
    val gClient by inject<GrunnbeløpClient>()

    @Test
    @Disabled
    internal fun name() = suspendableTest {
        DatapakkePublisherJob(
            repo,
            httpClient,
            gClient,
            "https://datakatalog-api.dev.intern.nav.no/v1/datapackage",
        "8f29efc4b7e41002130db5a172587fd4"
        ).doJob()
    }
}