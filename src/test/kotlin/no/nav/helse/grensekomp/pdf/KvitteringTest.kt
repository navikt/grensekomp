package no.nav.helse.grensekomp.pdf

import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import org.junit.jupiter.api.Test

import wiremock.com.google.common.io.Files
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertTrue

internal class KvitteringTest {

    @Test
    fun lagPDF() {
        val refusjonskrav = Refusjonskrav(
                opprettetAv = TestData.opprettetAv,
                identitetsnummer = TestData.validIdentitetsnummer,
                virksomhetsnummer = TestData.validOrgNr,
                perioder = setOf(
                    Periode(
                        LocalDate.of(2020, 4, 1),
                        LocalDate.of(2020, 4, 5),
                        2,
                        4500800.50
                ), Periode(
                        LocalDate.of(2020, 4, 5),
                        LocalDate.of(2020, 4, 10),
                        4,
                        1220800.50
                )),
                opprettet = LocalDateTime.now(),
                status = RefusjonskravStatus.MOTTATT
        )
        val kv = PDFGenerator()
        val ba = kv.lagPDF(refusjonskrav)
//        val file = File("kvittering_vanlig.pdf")
        val file = File.createTempFile("kvittering_vanlig", "pdf")
        Files.write(ba, file)
        assertTrue { file.exists() }
    }

    @Test
    fun norskeBokstaver() {
        val kv = PDFGenerator()
        val refusjonskrav = Refusjonskrav(
                opprettetAv = TestData.opprettetAv,
                identitetsnummer = TestData.validIdentitetsnummer,
                virksomhetsnummer = TestData.validOrgNr,
                perioder = setOf(Periode(
                        LocalDate.of(2020, 4, 1),
                        LocalDate.of(2020, 4, 5),
                        2,
                        250.50
                )),
                opprettet = LocalDateTime.now(),
                status = RefusjonskravStatus.MOTTATT
        )
        val ba = kv.lagPDF(refusjonskrav)
//        val file = File("kvittering_spesialtegn.pdf")
        val file = File.createTempFile("kvittering_spesialtegn", "pdf")
        Files.write(ba, file)
        assertTrue { file.exists() }
    }
}