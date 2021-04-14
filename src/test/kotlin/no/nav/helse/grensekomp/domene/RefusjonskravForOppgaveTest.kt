package no.nav.helse.grensekomp.domene

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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

        val om = ObjectMapper()
        om.registerModule(KotlinModule())
        om.registerModule(Jdk8Module())
        om.registerModule(JavaTimeModule())
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        om.configure(SerializationFeature.INDENT_OUTPUT, true)
        om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        assertThat(mapped.soekerForSegSelv).isTrue()
        println(om.writerWithDefaultPrettyPrinter().writeValueAsString(mapped))
    }
}