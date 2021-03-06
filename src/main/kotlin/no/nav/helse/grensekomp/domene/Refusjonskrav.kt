package no.nav.helse.grensekomp.domene

import java.time.LocalDateTime
import java.util.*

data class Refusjonskrav(
    var opprettetAv: String,
    val identitetsnummer: String,
    val virksomhetsnummer: String,
    val periode: Periode,
    val bekreftet: Boolean = true,
    var bostedland: String,

    var status: RefusjonskravStatus = RefusjonskravStatus.MOTTATT,
    val erEØSStatsborger: Boolean = true,
    val erDød: Boolean = false,
    val etteranmeldtArbeidsforhold: Boolean = false,
    var oppgaveId: String? = null,
    var joarkReferanse: String? = null,

    var sletteOppgaveId: String? = null,
    var sletteJoarkReferanse: String? = null,

    var kilde: String = "WEBSKJEMA",

    val opprettet: LocalDateTime = LocalDateTime.now(),
    val id: UUID = UUID.randomUUID(),
    var kvitteringId: UUID? = null
): Comparable<Refusjonskrav> {
    override fun compareTo(other: Refusjonskrav): Int {
        if (other.identitetsnummer > identitetsnummer)
            return -1
        if (identitetsnummer > other.identitetsnummer)
            return 1
        if (other.periode.fom.isAfter(periode.fom))
            return -1
        if (other.periode.fom.isBefore(periode.fom))
            return 1
        if (other.opprettet.isAfter(opprettet))
            return -1
        if (other.opprettet.isBefore(opprettet))
            return 1
        return 0
    }
}

enum class RefusjonskravStatus {
    MOTTATT,
    SENDT_TIL_BEHANDLING,
    AVBRUTT, // Denne er ment som en måte å skru av prosesseringen for krav som skal ignoreres, men ikke skal slettes
    SLETTET
}
