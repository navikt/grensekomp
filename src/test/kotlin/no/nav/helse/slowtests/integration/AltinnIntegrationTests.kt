package no.nav.helse.slowtests.integration

import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.integration.altinn.message.Clients
import no.nav.helse.grensekomp.kvittering.AltinnKvitteringMapper
import no.nav.helse.grensekomp.kvittering.AltinnKvitteringSender
import no.nav.helse.grensekomp.kvittering.Kvittering
import no.nav.helse.slowtests.systemtests.api.SystemTestBase
import org.junit.jupiter.api.Test
import org.koin.core.get
import java.time.LocalDateTime.now

class AltinnIntegrationTests : SystemTestBase() {

    @Test
    internal fun name() {
        val client = Clients.iCorrespondenceExternalBasic("https://tt02.altinn.no/ServiceEngineExternal/CorrespondenceAgencyExternalBasic.svc")
        val sender = AltinnKvitteringSender(
            AltinnKvitteringMapper("5534", get()),
            client,
            "NAV",
            "S3u4uSWa",
            get()
        )

        try {
            sender.send(Kvittering(virksomhetsnummer = TestData.validOrgNr, refusjonsListe = emptyList(), tidspunkt = now()))
        } catch(ex: Exception) {
            println(ex.message)
        }

    }
}