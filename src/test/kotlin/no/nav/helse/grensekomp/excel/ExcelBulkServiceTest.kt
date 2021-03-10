package no.nav.helse.grensekomp.excel

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.service.RefusjonskravService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ExcelBulkServiceTest {

    val excelFile = ExcelParserTest::class.java.classLoader.getResourceAsStream("komp_nav_TESTFILE.xlsx");
    val parserMock = mockk<ExcelParser>()
    val serviceMock = mockk<RefusjonskravService>()


    @Test
    internal fun `thrower ved feil i excelarket`() {
        val bulkservice = ExcelBulkService(serviceMock, parserMock)
        every { parserMock.parseAndValidateExcelContent(any(), TestData.validIdentitetsnummer) } returns ExcelParser.ExcelParsingResult(emptyList(), setOf(ExcelFileRowError(1,"test","test")))

        assertThrows<ExcelFileParsingException> { bulkservice.processExcelFile(excelFile, TestData.validIdentitetsnummer) }
    }

    @Test
    internal fun `Lagrer til databasen ved feilfri parsing`() {
        val bulkservice = ExcelBulkService(serviceMock, parserMock)
        val refusjonskrabParsedFromFile = listOf(
            Refusjonskrav(
                TestData.opprettetAv,
                TestData.validIdentitetsnummer,
                TestData.validOrgNr,
                emptySet()
        )
        )

        every { parserMock.parseAndValidateExcelContent(any(), TestData.validIdentitetsnummer) } returns ExcelParser.ExcelParsingResult(refusjonskrabParsedFromFile, emptySet())
        val refernceNumber = 123
        every { serviceMock.bulkInsert(refusjonskrabParsedFromFile) } returns listOf(refernceNumber)

       bulkservice.processExcelFile(excelFile, TestData.validIdentitetsnummer)

        verify(exactly = 1) { serviceMock.bulkInsert(refusjonskrabParsedFromFile) }
    }
}



