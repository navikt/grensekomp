package no.nav.helse.fritakagp.domain

import no.nav.helse.fritakagp.db.SimpleJsonbEntity
import java.time.LocalDateTime
import java.util.*

data class GravidKrav(
    override val id: UUID = UUID.randomUUID(),
    val opprettet: LocalDateTime = LocalDateTime.now(),
    val sendtAv: String,

    val virksomhetsnummer: String,
    val identitetsnummer: String,
    val periode: Arbeidsgiverperiode,
    val harVedlegg: Boolean = false,
    val kontrollDager: Int?,

    var journalpostId: String? = null,

    var oppgaveId: String? = null,
    var virksomhetsnavn: String? = null
) : SimpleJsonbEntity