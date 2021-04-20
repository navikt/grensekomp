package no.nav.helse.grensekomp.prosessering.refusjonskrav

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlIdent
import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import no.nav.helse.grensekomp.integration.GrunnbeløpClient
import no.nav.helse.grensekomp.metrics.*
import no.nav.helse.grensekomp.service.JoarkService
import no.nav.helse.grensekomp.service.OppgaveService
import no.nav.helse.grensekomp.utils.MDCOperations
import no.nav.helse.grensekomp.utils.withMDC
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*

class RefusjonskravProcessor(val joarkService: JoarkService,
                             val oppgaveService: OppgaveService,
                             val repository: RefusjonskravRepository,
                             val pdlClient: PdlClient,
                             val grunnbeløpClient: GrunnbeløpClient,
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
        if (refusjonskrav.status == RefusjonskravStatus.SENDT_TIL_BEHANDLING) {
            return
        }
        val timer = KRAV_TIME.startTimer()
        log.info("Prosesserer: ${refusjonskrav.id}")
        try {

            if (refusjonskrav.joarkReferanse.isNullOrBlank()) {
                refusjonskrav.joarkReferanse = joarkService.journalfør(refusjonskrav, callId)
                JOURNALFOERING_COUNTER.inc()
            }

            if (refusjonskrav.oppgaveId.isNullOrBlank()) {

                val aktørId = pdlClient.fullPerson(refusjonskrav.identitetsnummer)
                    ?.hentIdenter
                    ?.trekkUtIdent(PdlIdent.PdlIdentGruppe.AKTORID)
                    ?: throw IllegalStateException("Kunne ikke finne Aktørid for personen i kravet")

                refusjonskrav.oppgaveId = oppgaveService.opprettBehandlingsoppgave(
                        refusjonskrav,
                        refusjonskrav.joarkReferanse!!,
                        aktørId,
                        callId
                )
                OPPGAVE_COUNTER.inc()
            }
            refusjonskrav.status = RefusjonskravStatus.SENDT_TIL_BEHANDLING
        } finally {
            try {
                timer.close()
                repository.update(refusjonskrav)
                tryDoMetrics(refusjonskrav)
            } catch (t: Throwable) {
                logger.error("Feilet i lagring av ${refusjonskrav.id} med  joarkRef: ${refusjonskrav.joarkReferanse} oppgaveId ${refusjonskrav.oppgaveId} ")
                throw t
            }
        }
    }

    private fun tryDoMetrics(refusjonskrav: Refusjonskrav) {
        try {
            INNKOMMENDE_REFUSJONSKRAV_COUNTER.inc()
            INNKOMMENDE_REFUSJONSKRAV_BELOEP_COUNTER
                .labels(refusjonskrav.bostedland, refusjonskrav.erEØSStatsborger.toString())
                .inc(refusjonskrav.periode.estimertUtbetaling(grunnbeløpClient.hentGrunnbeløp().grunnbeløp * 6.0).toDouble())
        } catch (t: Throwable) {
            logger.error("Feilet i å telle metrikker", t)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RefusjonskravProcessor::class.java)
        val JOBB_TYPE = "refusjonskrav"

    }
}

data class RefusjonskravJobData(
        val kravId: UUID
)
