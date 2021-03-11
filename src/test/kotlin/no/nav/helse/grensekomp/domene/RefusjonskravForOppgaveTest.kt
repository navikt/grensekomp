package no.nav.helse.grensekomp.domene

import no.nav.helse.grensekomp.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RefusjonskravForOppgaveTest {
    @Test
    internal fun `sender informasjon til Oppgave om personer som søker på egne vegne`() {
        val mapped = TestData.gyldigKrav.copy(
                opprettetAv = TestData.validIdentitetsnummer,
                identitetsnummer = TestData.validIdentitetsnummer
        ).toRefusjonskravForOppgave()

        assertThat(mapped.soekerForSegSelv).isTrue()
    }
}