package no.nav.helse.grensekomp.service

import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.*
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.pdf.PDFGenerator
import java.util.*

class JoarkService(val dokarkivKlient: DokarkivKlient) {
    val pdfGenerator = PDFGenerator()

    fun journalfør(refusjonskrav: Refusjonskrav, callId: String): String {
        val base64EnkodetPdf = Base64.getEncoder().encodeToString(pdfGenerator.lagPDF(refusjonskrav))
        return dokarkivKlient.journalførDokument(
                JournalpostRequest(
                        journalposttype = Journalposttype.INNGAAENDE,
                        kanal = "NAV_NO",
                        eksternReferanseId = refusjonskrav.id.toString(),
                        tittel = "Refusjonskrav for utestengt arbeidsgtaker",
                        bruker = Bruker(
                                id = refusjonskrav.identitetsnummer,
                                idType = IdType.FNR
                        ),
                        avsenderMottaker = AvsenderMottaker(
                                id = refusjonskrav.virksomhetsnummer,
                                idType = IdType.ORGNR,
                                navn = "Arbeidsgiver"
                        ),
                        dokumenter = listOf(Dokument(
                                brevkode = "refusjonskrav_utestengt_arbeider_korona",
                                tittel = "Refusjonskrav utestengt arbeider korona",
                                dokumentVarianter = listOf(DokumentVariant(
                                        fysiskDokument = base64EnkodetPdf
                                ))
                        )),
                        datoMottatt = refusjonskrav.opprettet.toLocalDate()
                ), true, callId).journalpostId
    }
}