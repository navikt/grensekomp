package no.nav.helse.grensekomp.domene
import java.time.LocalDateTime
import java.util.*

data class RefusjonskravForOppgave(
        val identitetsnummer: String,
        val virksomhetsnummer: String,
        val periode: Periode,

        var status: RefusjonskravStatus = RefusjonskravStatus.MOTTATT,
        var feilmelding: String? = null,
        var oppgaveId: String? = null,
        var joarkReferanse: String? = null,
        var kilde: String = "WEBSKJEMA",

        val opprettet: LocalDateTime = LocalDateTime.now(),
        val id: UUID = UUID.randomUUID(),
        var kvitteringId: UUID? = null,
        var indeksertInflux: Boolean = false,

        val soekerForSegSelv: Boolean
)
fun Refusjonskrav.toRefusjonskravForOppgave() = RefusjonskravForOppgave(
        identitetsnummer = identitetsnummer,
        virksomhetsnummer = virksomhetsnummer,
        periode = periode,
        status = status,
        feilmelding = feilmelding,
        oppgaveId = oppgaveId,
        joarkReferanse = joarkReferanse,
        kilde = kilde,
        opprettet = opprettet,
        id = id,
        kvitteringId = kvitteringId,
        indeksertInflux = indeksertInflux,
        soekerForSegSelv = identitetsnummer == opprettetAv
)
