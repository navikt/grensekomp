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
import no.nav.helse.grensekomp.service.JoarkService
import no.nav.helse.grensekomp.service.OppgaveService
import no.nav.helse.grensekomp.utils.MDCOperations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.time.LocalDate.now

class RefusjonskravProcessorTest {
    val joarkMock = mockk<JoarkService>(relaxed = true)
    val oppgaveMock = mockk<OppgaveService>(relaxed = true)
    val repositoryMock = mockk<PostgresRefusjonskravRepository>(relaxed = true)
    val pdlClientMock = mockk<PdlClient>(relaxed = true)
    val grunnbeløpMock = mockk<GrunnbeløpClient>(relaxed = true)
    val refusjonskravBehandler = RefusjonskravProcessor(joarkMock, oppgaveMock, repositoryMock, pdlClientMock, grunnbeløpMock, ObjectMapper())
    lateinit var refusjonskrav: Refusjonskrav

    @BeforeEach
    fun setup() {
        every { grunnbeløpMock.hentGrunnbeløp() } returns GrunnbeløpInfo(now(), 1000, 100, 100, 1.2)

        refusjonskrav = Refusjonskrav(
                opprettetAv = "123",
                identitetsnummer = "123",
                virksomhetsnummer = "213",
                periode = TestData.gyldigKrav.periode,
                status = RefusjonskravStatus.MOTTATT,
                bostedland = "SWE"
        )
    }

    @Test
    fun `skal ikke journalføre når det allerede foreligger en journalpostId `() {
        refusjonskrav.joarkReferanse = "joark"
        refusjonskravBehandler.behandle(refusjonskrav)
        verify(exactly = 0) { joarkMock.journalfør(any(), MDCOperations.generateCallId()) }
    }

    @Test
    fun `skal ikke lage oppgave når det allerede foreligger en oppgaveId `() {
        refusjonskrav.oppgaveId = "ppggssv"
        refusjonskravBehandler.behandle(refusjonskrav)
        verify(exactly = 0) { oppgaveMock.opprettBehandlingsoppgave(any(), any(), any(), MDCOperations.generateCallId()) }
    }

    @Test
    fun `skal journalføre, opprette oppgave og oppdatere kravet i databasen`() {
        val joarkref = "joarkref"
        val opgref = "oppgaveref"
        val aktørId = "aktørId"

        every { joarkMock.journalfør(refusjonskrav, any()) } returns joarkref
        every { pdlClientMock.fullPerson(any()) } returns PdlHentFullPerson(null, PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent(aktørId, PdlIdent.PdlIdentGruppe.AKTORID))), null)

        every { oppgaveMock.opprettBehandlingsoppgave(refusjonskrav, joarkref, aktørId, any()) } returns opgref

        refusjonskravBehandler.behandle(refusjonskrav)

        assertThat(refusjonskrav.status).isEqualTo(RefusjonskravStatus.SENDT_TIL_BEHANDLING)
        assertThat(refusjonskrav.joarkReferanse).isEqualTo(joarkref)
        assertThat(refusjonskrav.oppgaveId).isEqualTo(opgref)

        verify(exactly = 1) { joarkMock.journalfør(any(), any()) }
        verify(exactly = 1) { oppgaveMock.opprettBehandlingsoppgave(any(), any(), any(), any()) }
        verify(exactly = 1) { repositoryMock.update(refusjonskrav) }
    }


    @Test
    fun `Ved feil skal kravet fortsatt ha status MOTTATT og joarkref om det finnes  og kaste exception oppover`() {
        val joarkref = "joarkref"
        val aktørId = "aktørId"

        every { joarkMock.journalfør(refusjonskrav, any()) } returns joarkref
        every { pdlClientMock.fullPerson(any()) } returns PdlHentFullPerson(null, PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent(aktørId, PdlIdent.PdlIdentGruppe.AKTORID))), null)

        every { oppgaveMock.opprettBehandlingsoppgave(refusjonskrav, joarkref, aktørId, any()) } throws IOException()

        assertThrows<IOException> { refusjonskravBehandler.behandle(refusjonskrav) }

        assertThat(refusjonskrav.status).isEqualTo(RefusjonskravStatus.MOTTATT)
        assertThat(refusjonskrav.joarkReferanse).isEqualTo(joarkref)
        assertThat(refusjonskrav.oppgaveId).isNull()

        verify(exactly = 1) { joarkMock.journalfør(any(), any()) }
        verify(exactly = 1) { oppgaveMock.opprettBehandlingsoppgave(any(), any(), any(), any()) }
        verify(exactly = 1) { repositoryMock.update(refusjonskrav) }
    }
}
