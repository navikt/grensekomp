package no.nav.helse.grensekomp.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.*
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.integration.brreg.BrregClient
import no.nav.helse.grensekomp.pdf.PDFGenerator
import java.util.*

class JoarkService(val dokarkivKlient: DokarkivKlient, val brreg: BrregClient, val om: ObjectMapper) {
    val pdfGenerator = PDFGenerator()

    fun journalfør(refusjonskrav: Refusjonskrav, callId: String): String {
        val virksomhetsNavn = runBlocking {
            brreg.getVirksomhetsNavn(refusjonskrav.virksomhetsnummer)
        }
        val base64EnkodetPdf = Base64.getEncoder().encodeToString(pdfGenerator.lagPDF(refusjonskrav, virksomhetsNavn))
        val base64EnkodetJson = Base64.getEncoder().encodeToString(om.writeValueAsBytes(refusjonskrav))

        return dokarkivKlient.journalførDokument(
                JournalpostRequest(
                        journalposttype = Journalposttype.INNGAAENDE,
                        kanal = "NAV_NO",
                        eksternReferanseId = refusjonskrav.id.toString(),
                        tittel = "Refusjonskrav for utestengt arbeidstaker",
                        bruker = Bruker(
                                id = refusjonskrav.identitetsnummer,
                                idType = IdType.FNR
                        ),
                        avsenderMottaker = AvsenderMottaker(
                                id = refusjonskrav.virksomhetsnummer,
                                idType = IdType.ORGNR,
                                navn = virksomhetsNavn
                        ),
                        dokumenter = listOf(Dokument(
                                brevkode = "refusjonskrav_utestengt_arbeider_korona",
                                tittel = "Refusjonskrav utestengt arbeider korona",
                                dokumentVarianter = listOf(
                                    DokumentVariant(fysiskDokument = base64EnkodetPdf),
                                    DokumentVariant(fysiskDokument = base64EnkodetJson, filtype = "JSON", variantFormat = "ORIGINAL")
                                )
                        )),
                        datoMottatt = refusjonskrav.opprettet.toLocalDate()
                ), true, callId).journalpostId
    }

    fun journalførSletting(refusjonskrav: Refusjonskrav, callId: String): String {
        val base64EnkodetPdf = Base64.getEncoder().encodeToString(pdfGenerator.lagSlettingPDF(refusjonskrav))
        val base64EnkodetJson = Base64.getEncoder().encodeToString(om.writeValueAsBytes(refusjonskrav))
        val virksomhetsNavn = runBlocking {
            brreg.getVirksomhetsNavn(refusjonskrav.virksomhetsnummer)
        }

        return dokarkivKlient.journalførDokument(
                JournalpostRequest(
                        journalposttype = Journalposttype.INNGAAENDE,
                        kanal = "NAV_NO",
                        eksternReferanseId = "${refusjonskrav.id}-annul",
                        tittel = "Annuller refusjonskrav for utestengt arbeidstaker",
                        bruker = Bruker(
                                id = refusjonskrav.identitetsnummer,
                                idType = IdType.FNR
                        ),
                        avsenderMottaker = AvsenderMottaker(
                                id = refusjonskrav.virksomhetsnummer,
                                idType = IdType.ORGNR,
                                navn = virksomhetsNavn
                        ),
                        dokumenter = listOf(Dokument(
                                brevkode = "annuller_refusjonskrav_utestengt_arbeider_korona",
                                tittel = "Annullering av refusjonskrav utestengt arbeider korona",
                                dokumentVarianter = listOf(
                                    DokumentVariant(fysiskDokument = base64EnkodetPdf),
                                    DokumentVariant(fysiskDokument = base64EnkodetJson, filtype = "JSON", variantFormat = "ORIGINAL")
                                )
                        )),
                        datoMottatt = refusjonskrav.opprettet.toLocalDate()
                ), true, callId).journalpostId
    }
}