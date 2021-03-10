package no.nav.helse.grensekomp.kvittering

import no.nav.helse.grensekomp.domene.Refusjonskrav
import java.time.LocalDateTime
import java.util.*

data class Kvittering(
    val id: UUID = UUID.randomUUID(),
    val virksomhetsnummer: String,
    val refusjonsListe: List<Refusjonskrav>,
    val tidspunkt: LocalDateTime,
    var status: KvitteringStatus = KvitteringStatus.OPPRETTET
)

enum class KvitteringStatus {
    OPPRETTET,
    SENDT,
    FEILET,
    JOBB
}
