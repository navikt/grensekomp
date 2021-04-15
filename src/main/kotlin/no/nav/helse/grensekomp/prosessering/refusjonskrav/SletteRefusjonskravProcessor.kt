package no.nav.helse.grensekomp.prosessering.refusjonskrav

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlIdent
import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.metrics.*
import no.nav.helse.grensekomp.service.JoarkService
import no.nav.helse.grensekomp.service.OppgaveService
import no.nav.helse.grensekomp.utils.MDCOperations
import no.nav.helse.grensekomp.utils.withMDC
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException

class SletteRefusjonskravProcessor(val joarkService: JoarkService,
                                   val oppgaveService: OppgaveService,
                                   val repository: RefusjonskravRepository,
                                   val pdlClient: PdlClient,
                                   val om: ObjectMapper) : BakgrunnsjobbProsesserer {

    val logger = LoggerFactory.getLogger(RefusjonskravProcessor::class.java)
    override val type: String get() = JOBB_TYPE

    override fun prosesser(jobb: Bakgrunnsjobb) {
        val refusjonskravJobbData = om.readValue(jobb.data, RefusjonskravJobData::class.java)
        repository.getById(refusjonskravJobbData.kravId)?.let { behandle(it) }
    }


    fun behandle(refusjonskrav: Refusjonskrav) {
        val callId = MDCOperations.generateCallId()
        withMDC(mapOf("x_call_id" to callId)) {
            behandle(refusjonskrav, callId)
        }
    }

    private fun behandle(refusjonskrav: Refusjonskrav, callId: String) {
        val timer = KRAV_TIME.startTimer()
        log.info("Prosessere slettingr: ${refusjonskrav.id}")
        try {
            if (refusjonskrav.sletteJoarkReferanse.isNullOrBlank()) {
                refusjonskrav.sletteJoarkReferanse = joarkService.journalførSletting(refusjonskrav, callId)
            }

            if (refusjonskrav.sletteOppgaveId.isNullOrBlank()) {

                val aktørId = pdlClient.fullPerson(refusjonskrav.identitetsnummer)
                    ?.hentIdenter
                    ?.trekkUtIdent(PdlIdent.PdlIdentGruppe.AKTORID)
                    ?: throw IllegalStateException("Kunne ikke finne Aktørid for personen i kravet")

                refusjonskrav.sletteOppgaveId = oppgaveService.opprettSletteBehandlingsoppgave(
                        refusjonskrav,
                        refusjonskrav.sletteJoarkReferanse!!,
                        aktørId,
                        callId
                )
            }
        } finally {
            try {
                timer.close()
                repository.update(refusjonskrav)
                tryDoMetrics()
            } catch (t: Throwable) {
                logger.error("Feilet i lagring av ${refusjonskrav.id} ved sletting. JoarkRef: ${refusjonskrav.sletteJoarkReferanse} oppgaveId ${refusjonskrav.sletteOppgaveId} ")
                throw t
            }
        }
    }

    private fun tryDoMetrics() {
        ANNULLERTE_REFUSJONSKRAV.inc()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SletteRefusjonskravProcessor::class.java)
        val JOBB_TYPE = "slett-refusjonskrav"
    }
}