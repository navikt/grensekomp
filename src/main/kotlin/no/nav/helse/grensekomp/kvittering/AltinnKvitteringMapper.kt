package no.nav.helse.grensekomp.kvittering

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.nav.helse.grensekomp.integration.GrunnbeløpClient
import no.nav.helse.grensekomp.pdf.PDFGenerator.Companion.NUMBER_FORMAT
import java.time.format.DateTimeFormatter

class AltinnKvitteringMapper(
    val altinnTjenesteKode: String,
    val grunnbeløpClient: GrunnbeløpClient) {


    fun mapKvitteringTilInsertCorrespondence(kvittering: Kvittering): InsertCorrespondenceV2 {
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dateTimeFormatterMedKl = DateTimeFormatter.ofPattern("dd.MM.yyyy 'kl.' HH:mm")
        val dateTimeFormatterPlain = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val seksG = grunnbeløpClient.hentGrunnbeløp().grunnbeløp * 6

        val tittel = "Kvittering for krav om utvidet refusjon ved koronaviruset"

        val innhold = """            <html>
               <head>
                   <meta charset="UTF-8">
               </head>
               <body>
                   <div class="melding">
                       <h2>Kvittering  – refusjonskrav ved innreiseforbud</h2>
                       <p><strong>Virksomhetsnummer</strong> ${kvittering.virksomhetsnummer}<p>
                       <p>${kvittering.tidspunkt.format(dateTimeFormatterMedKl)}</p>
                       <p></p>
                       <p>
                        Søknaden vil bli behandlet raskt dersom alt er i orden. Har dere spørsmål, ring NAVs arbeidsgivertelefon <br><strong>55 55 33 36.</strong>
                        </p>
                        <p></p>
                        <h3>Dere har innrapportert følgende: </h3>
                        <table style="border-style:solid; border-color:rgba(64, 56, 50, 1); border-width:2px;">
                            <tr style="border-style:solid; border-color:rgba(64, 56, 50, 1); border-width:2px;">
                                <th style="padding:12px">Mottatt</th>
                                <th style="padding:12px">Fødsels-/D-nummer</th>
                                <th style="padding:12px">Periode</th>
                                <th style="padding:12px">Grunnlag</th>
                                <th style="padding:12px">Omregnet til 70 %</th>
                            </tr>
                        ${
            kvittering.refusjonsListe.sorted().joinToString(separator = "") { krav ->
                """
                                <tr>
                                <td style="padding:12px">${krav.opprettet.format(dateTimeFormatterPlain)}</td>    
                                <td style="padding:12px">${krav.identitetsnummer}</td>
                                <td style="padding:12px">${krav.periode.fom.format(dateFormatter)} - ${krav.periode.tom.format(dateFormatter)}</td>
                                <td style="padding:12px">${krav.periode.beregnetMånedsinntekt}</td>
                                <td style="padding:12px">${NUMBER_FORMAT.format(krav.periode.estimertUtbetaling(seksG))}</td>
                                </tr>
                                        """.trimIndent()
            }
        }}
                       </table>
                   </div>
               </body>
            </html>
""".trimIndent()


        val meldingsInnhold = ExternalContentV2()
            .withLanguageCode("1044")
            .withMessageTitle(tittel)
            .withMessageBody(innhold)
            .withMessageSummary("Kvittering for krav om utvidet refusjon ved koronaviruset")


        return InsertCorrespondenceV2()
            .withAllowForwarding(false)
            .withReportee(kvittering.virksomhetsnummer)
            .withMessageSender("NAV (Arbeids- og velferdsetaten)")
            .withServiceCode(altinnTjenesteKode)
            .withServiceEdition("1")
            .withContent(meldingsInnhold)

    }

}
