package no.nav.helse.grensekomp.pdf

import no.nav.helse.grensekomp.domene.Refusjonskrav
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PDFGenerator {

    private val FONT_SIZE = 11f
    private val LINE_HEIGHT = 15f
    private val MARGIN_X = 40f
    private val MARGIN_Y = 40f
    private val FONT_NAME = "fonts/SourceSansPro-Regular.ttf"

    fun lagPDF(refusjonskrav: Refusjonskrav): ByteArray {
        val doc = PDDocument()
        val page = PDPage()
        val font = PDType0Font.load(doc, this::class.java.classLoader.getResource(FONT_NAME).openStream())
        doc.addPage(page)
        val contentStream = PDPageContentStream(doc, page)
        contentStream.beginText()
        contentStream.setFont(font, FONT_SIZE)
        val mediaBox = page.mediaBox
        val startX = mediaBox.lowerLeftX + MARGIN_X
        val startY = mediaBox.upperRightY - MARGIN_Y
        contentStream.newLineAtOffset(startX, startY)
        contentStream.showText("Fødselsnummer: ${refusjonskrav.identitetsnummer}")
        contentStream.newLineAtOffset(0F, -LINE_HEIGHT)
        contentStream.showText("Virksomhetsnummer: ${refusjonskrav.virksomhetsnummer}")
        contentStream.newLineAtOffset(0F, -LINE_HEIGHT * 2)
        contentStream.showText("Periode:")
        contentStream.newLineAtOffset(0F, -LINE_HEIGHT)
        contentStream.showText("Fra: ${Companion.DATE_FORMAT.format(refusjonskrav.periode.fom)}    Til: ${Companion.DATE_FORMAT.format(refusjonskrav.periode.tom)}    Antall dager: ${refusjonskrav.periode.antallDagerMedRefusjon}    Refusjonsbeløp: ${Companion.NUMBER_FORMAT.format(refusjonskrav.periode.dagsats)}")
        contentStream.newLineAtOffset(0F, -LINE_HEIGHT * 2)
        contentStream.showText("Opprettet: ${Companion.TIMESTAMP_FORMAT.format(LocalDateTime.now())}")
        contentStream.endText()
        contentStream.close()
        val out = ByteArrayOutputStream()
        doc.save(out)
        val ba = out.toByteArray()
        doc.close()
        return ba
    }

    companion object {
        val NUMBER_FORMAT = DecimalFormat("#,###.00")
        val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    }

}