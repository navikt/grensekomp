package no.nav.helse.grensekomp.integrasjon

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlient
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveRequest
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveResponse
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.koin.common
import no.nav.helse.grensekomp.service.OppgaveService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

internal class OppgaveServiceTest : KoinTest {
    val oppgaveKlientMock = mockk<OppgaveKlient>()

    val oppgaveService = OppgaveService(oppgaveKlientMock)

    private val mockAktørId = "aktør-id"
    private val mockJoarkRef = "joark-ref"
    private val mockOppgaveId = 4329

    @Test
    fun `Mapper krav og sender oppgaveopprettelse`() {
        val mappedRequest = slot<OpprettOppgaveRequest>()


        coEvery { oppgaveKlientMock.opprettOppgave(capture(mappedRequest), any()) } returns OpprettOppgaveResponse(
            mockOppgaveId
        )

        val result = oppgaveService.opprettOppgave(
            TestData.gyldigKrav,
            mockJoarkRef,
            mockAktørId,
            "call-id"
        )

        coVerify(exactly = 1) { oppgaveKlientMock.opprettOppgave(any(), any()) }

        assertThat(result).isEqualTo("$mockOppgaveId")

        assertThat(mappedRequest.isCaptured).isTrue()
        assertThat(mappedRequest.captured.aktoerId).isEqualTo(mockAktørId)
        assertThat(mappedRequest.captured.journalpostId).isEqualTo(mockJoarkRef)
        assertThat(mappedRequest.captured.beskrivelse).isNotEmpty()
    }
}