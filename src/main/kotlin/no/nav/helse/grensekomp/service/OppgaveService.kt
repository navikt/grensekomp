package no.nav.helse.grensekomp.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlient
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveRequest
import no.nav.helse.grensekomp.domene.Refusjonskrav
import java.time.LocalDate

class OppgaveService(private val oppgaveKlient: OppgaveKlient) {

    fun opprettOppgave(refusjonskrav: Refusjonskrav, journalpostId: String, aktørId: String, callId: String): String {
        val response = runBlocking {
            val request = mapOppgave(journalpostId, aktørId, mapStrukturert(refusjonskrav))
            oppgaveKlient.opprettOppgave(request, callId)
        }
        return "${response.id}"
    }

    private fun mapStrukturert(refusjonskrav: Refusjonskrav): String {
        return """
            Krav om refusjon av lønn for utestengte EØS-borgere
            
            Arbeidstaker: ${refusjonskrav.identitetsnummer}
            Arbeidsgiver: ${refusjonskrav.virksomhetsnummer}
            
            Periode: ${refusjonskrav.perioder.first().fom} - ${refusjonskrav.perioder.first().tom}
            Antall dager det kreves refusjon for: ${refusjonskrav.perioder.first().antallDagerMedRefusjon}
            Refusjonskrav : ${refusjonskrav.perioder.first().beloep} NOK
        """.trimIndent()
    }

    private fun mapOppgave(journalpostId: String, aktørId: String, beskrivelse: String): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
                aktoerId = aktørId,
                journalpostId = journalpostId,
                beskrivelse = beskrivelse,
                tema = "SYK",
                oppgavetype = "BEH_SAK",
                behandlingstema = "ab0433",
                aktivDato = LocalDate.now(),
                fristFerdigstillelse = LocalDate.now().plusDays(7),
                prioritet = "NORM"
        )
    }
}