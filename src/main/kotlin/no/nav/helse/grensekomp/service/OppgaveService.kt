package no.nav.helse.grensekomp.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlient
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveRequest
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.toRefusjonskravForOppgave
import java.time.LocalDate

class OppgaveService(private val oppgaveKlient: OppgaveKlient, private val om: ObjectMapper) {

    fun opprettBehandlingsoppgave(refusjonskrav: Refusjonskrav, journalpostId: String, aktørId: String, callId: String): String {
        val response = runBlocking {
            val request = mapBehandlingsoppgave(journalpostId, aktørId, mapStrukturert(refusjonskrav))
            oppgaveKlient.opprettOppgave(request, callId)
        }
        return "${response.id}"
    }

    fun opprettSletteBehandlingsoppgave(refusjonskrav: Refusjonskrav, journalpostId: String, aktørId: String, callId: String): String {
        val response = runBlocking {
            val request = mapAnnulleringssoppgave(journalpostId, aktørId, mapStrukturert(refusjonskrav))
            oppgaveKlient.opprettOppgave(request, callId)
        }
        return "${response.id}"
    }

    private fun mapStrukturert(refusjonskrav: Refusjonskrav): String {
        val kravForOppgave = refusjonskrav.toRefusjonskravForOppgave()
        return om.writeValueAsString(kravForOppgave)
    }

    private fun mapBehandlingsoppgave(journalpostId: String, aktørId: String, beskrivelse: String): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
            aktoerId = aktørId,
            journalpostId = journalpostId,
            beskrivelse = beskrivelse,
            tema = "SYK",
            oppgavetype = "ROB_BEH", // For Krav: ROB_BEH

            behandlingstema = "ab0447", // kalles også "gjelder" felt
            aktivDato = LocalDate.now(),

            fristFerdigstillelse = LocalDate.now().plusDays(7),
            prioritet = "NORM"
        )
    }

    private fun mapAnnulleringssoppgave(journalpostId: String, aktørId: String, beskrivelse: String): OpprettOppgaveRequest {
        return OpprettOppgaveRequest(
            aktoerId = aktørId,
            journalpostId = journalpostId,
            beskrivelse = beskrivelse,
            tema = "SYK",
            oppgavetype = "VUR_KONS_YTE", // Vurder konsekvens for ytelse

            behandlingstema = "ab0448",
            aktivDato = LocalDate.now(),

            fristFerdigstillelse = LocalDate.now().plusDays(7),
            prioritet = "NORM"
        )
    }
}