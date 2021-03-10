package no.nav.helse.grensekomp.processing.gravid.soeknad

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.grensekomp.GravidSoeknadMetrics
import no.nav.helse.grensekomp.db.GravidSoeknadRepository
import java.util.*

class GravidSoeknadKvitteringProcessor(
    private val gravidSoeknadKvitteringSender: GravidSoeknadKvitteringSender,
    private val db: GravidSoeknadRepository,
    private val om: ObjectMapper
) : BakgrunnsjobbProsesserer {

    companion object {
        val JOB_TYPE = "gravid-søknad-altinn-kvittering"
    }
    override val type: String get() = JOB_TYPE


    override fun prosesser(jobb: Bakgrunnsjobb) {
        val kvitteringJobbData = om.readValue(jobb.data, Jobbdata::class.java)
        val soeknad = db.getById(kvitteringJobbData.soeknadId)
            ?: throw IllegalArgumentException("Fant ikke søknaden i jobbdatanene ${jobb.data}")

        gravidSoeknadKvitteringSender.send(soeknad)
        GravidSoeknadMetrics.tellKvitteringSendt()
    }

    data class Jobbdata(
        val soeknadId: UUID
    )
}
