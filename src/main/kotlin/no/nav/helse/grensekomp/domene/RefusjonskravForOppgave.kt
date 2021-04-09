package no.nav.helse.grensekomp.domene
import java.time.LocalDateTime
import java.util.*

data class RefusjonskravForOppgave(
        val id: UUID,
        val status: RefusjonskravStatus,
        val identitetsnummer: String,
        val virksomhetsnummer: String,
        val bostedland: String,

        val periode: Periode,

        val erEØSStatsborger: Boolean,

        val opprettet: LocalDateTime,

        val soekerForSegSelv: Boolean
)
fun Refusjonskrav.toRefusjonskravForOppgave() = RefusjonskravForOppgave(
        id = id,
        status = status,
        identitetsnummer = identitetsnummer,
        virksomhetsnummer = virksomhetsnummer,
        bostedland = bostedland,

        periode = periode,
        opprettet = opprettet,
        soekerForSegSelv = identitetsnummer == opprettetAv,
        erEØSStatsborger = erEØSStatsborger
)
