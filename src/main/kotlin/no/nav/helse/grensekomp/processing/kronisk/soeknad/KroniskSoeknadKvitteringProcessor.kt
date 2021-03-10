package no.nav.helse.grensekomp.processing.kronisk.soeknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.grensekomp.KroniskSoeknadMetrics
import no.nav.helse.grensekomp.db.KroniskSoeknadRepository
import java.util.*

class KroniskSoeknadKvitteringProcessor(
    private val kroniskSoeknadKvitteringSender: KroniskSoeknadKvitteringSender,
    private val db: KroniskSoeknadRepository,
    private val om: ObjectMapper
) : BakgrunnsjobbProsesserer {

    companion object {
        val JOB_TYPE = "kronisk-søknad-altinn-kvittering"
    }
    override val type: String get() = JOB_TYPE

    override fun prosesser(jobb: Bakgrunnsjobb) {
        val kvitteringJobbData: Jobbdata = om.readValue(jobb.data)
        val soeknad = db.getById(kvitteringJobbData.soeknadId)
            ?: throw IllegalArgumentException("Fant ikke søknaden i jobbdatanene ${jobb.data}")

        kroniskSoeknadKvitteringSender.send(soeknad)
        KroniskSoeknadMetrics.tellKvitteringSendt()
    }

    data class Jobbdata(
        val soeknadId: UUID
    )
}
