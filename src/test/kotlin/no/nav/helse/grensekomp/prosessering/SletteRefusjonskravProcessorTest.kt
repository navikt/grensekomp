package no.nav.helse.grensekomp.prosessering

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlHentFullPerson
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlIdent
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.db.PostgresRefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import no.nav.helse.grensekomp.integration.GrunnbeløpClient
import no.nav.helse.grensekomp.integration.GrunnbeløpInfo
import no.nav.helse.grensekomp.prosessering.refusjonskrav.RefusjonskravProcessor
import no.nav.helse.grensekomp.prosessering.refusjonskrav.SletteRefusjonskravProcessor
import no.nav.helse.grensekomp.service.JoarkService
import no.nav.helse.grensekomp.service.OppgaveService
import no.nav.helse.grensekomp.utils.MDCOperations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.time.LocalDate.now

class SletteRefusjonskravProcessorTest {
    val joarkMock = mockk<JoarkService>(relaxed = true)
    val oppgaveMock = mockk<OppgaveService>(relaxed = true)
    val repositoryMock = mockk<PostgresRefusjonskravRepository>(relaxed = true)
    val pdlClientMock = mockk<PdlClient>(relaxed = true)
    val refusjonskravBehandler = SletteRefusjonskravProcessor(joarkMock, oppgaveMock, repositoryMock, pdlClientMock, ObjectMapper())
    lateinit var refusjonskrav: Refusjonskrav

    @BeforeEach
    fun setup() {

        refusjonskrav = Refusjonskrav(
                opprettetAv = "123",
                identitetsnummer = "123",
                virksomhetsnummer = "213",
                periode = TestData.gyldigKrav.periode,
                status = RefusjonskravStatus.SLETTET,
                bostedland = "SWE",
                oppgaveId = "oppgaveID-org",
                joarkReferanse = "joark-org"
        )
    }

    @Test
    fun `skal ikke journalføre når det allerede foreligger en journalpostId `() {
        refusjonskrav.sletteJoarkReferanse = "joark"
        refusjonskravBehandler.behandle(refusjonskrav)
        verify(exactly = 0) { joarkMock.journalførSletting(any(), MDCOperations.generateCallId()) }
    }

    @Test
    fun `skal ikke lage oppgave når det allerede foreligger en oppgaveId `() {
        refusjonskrav.sletteOppgaveId = "ppggssv"
        refusjonskravBehandler.behandle(refusjonskrav)
        verify(exactly = 0) { oppgaveMock.opprettSletteBehandlingsoppgave(any(), any(), any(), MDCOperations.generateCallId()) }
    }

    @Test
    fun `skal journalføre, opprette oppgave og oppdatere kravet i databasen`() {
        val joarkref = "joarkref-slette"
        val opgref = "oppgaveref-slette"
        val aktørId = "aktørId"

        every { joarkMock.journalførSletting(refusjonskrav, any()) } returns joarkref
        every { pdlClientMock.fullPerson(any()) } returns PdlHentFullPerson(null, PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent(aktørId, PdlIdent.PdlIdentGruppe.AKTORID))), null)

        every { oppgaveMock.opprettSletteBehandlingsoppgave(refusjonskrav, joarkref, aktørId, any()) } returns opgref

        refusjonskravBehandler.behandle(refusjonskrav)

        assertThat(refusjonskrav.status).isEqualTo(RefusjonskravStatus.SLETTET)
        assertThat(refusjonskrav.sletteJoarkReferanse).isEqualTo(joarkref)
        assertThat(refusjonskrav.sletteOppgaveId).isEqualTo(opgref)

        verify(exactly = 1) { joarkMock.journalførSletting(any(), any()) }
        verify(exactly = 1) { oppgaveMock.opprettSletteBehandlingsoppgave(any(), any(), any(), any()) }
        verify(exactly = 1) { repositoryMock.update(refusjonskrav) }
    }


    @Test
    fun `Ved feil skal kravet fortsatt ha status SLETTET og joarkref om det finnes  og kaste exception oppover`() {
        val joarkref = "joarkref"
        val aktørId = "aktørId"

        every { joarkMock.journalførSletting(refusjonskrav, any()) } returns joarkref
        every { pdlClientMock.fullPerson(any()) } returns PdlHentFullPerson(null, PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent(aktørId, PdlIdent.PdlIdentGruppe.AKTORID))), null)

        every { oppgaveMock.opprettSletteBehandlingsoppgave(refusjonskrav, joarkref, aktørId, any()) } throws IOException()

        assertThrows<IOException> { refusjonskravBehandler.behandle(refusjonskrav) }

        assertThat(refusjonskrav.status).isEqualTo(RefusjonskravStatus.SLETTET)
        assertThat(refusjonskrav.sletteJoarkReferanse).isEqualTo(joarkref)
        assertThat(refusjonskrav.sletteOppgaveId).isNull()

        verify(exactly = 1) { joarkMock.journalførSletting(any(), any()) }
        verify(exactly = 1) { oppgaveMock.opprettSletteBehandlingsoppgave(any(), any(), any(), any()) }
        verify(exactly = 1) { repositoryMock.update(refusjonskrav) }
    }
}
