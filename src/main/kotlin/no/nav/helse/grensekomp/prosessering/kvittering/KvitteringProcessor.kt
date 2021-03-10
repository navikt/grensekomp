package no.nav.helse.grensekomp.prosessering.kvittering

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.grensekomp.db.KvitteringRepository
import no.nav.helse.grensekomp.kvittering.KvitteringSender
import java.util.*

class KvitteringProcessor(
    val kvitteringSender: KvitteringSender,
    val db: KvitteringRepository,
    val om: ObjectMapper
) : BakgrunnsjobbProsesserer {

    companion object {
        val JOBB_TYPE = "kvittering"
    }

    override val type: String get() = JOBB_TYPE

    override fun prosesser(jobb: Bakgrunnsjobb) {
        val kvitteringJobbData = om.readValue(jobb.data, KvitteringJobData::class.java)
        val kvittering = db.getById(kvitteringJobbData.kvitteringId)
        kvittering?.let { kvitteringSender.send(it) }
    }
}

data class KvitteringJobData(
        val kvitteringId: UUID
)