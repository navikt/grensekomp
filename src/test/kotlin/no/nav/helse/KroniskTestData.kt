package no.nav.helse

import no.nav.helse.fritakagp.domain.*
import no.nav.helse.fritakagp.web.api.resreq.KroniskKravRequest
import no.nav.helse.fritakagp.web.api.resreq.KroniskSoknadRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object KroniskTestData {
    val validIdentitetsnummer = "20015001543"
    val validOrgNr = "917404437"

    val soeknadKronisk = KroniskSoeknad(
        virksomhetsnummer = validOrgNr,
        identitetsnummer = validIdentitetsnummer,
        arbeidstyper = setOf(ArbeidsType.KREVENDE, ArbeidsType.MODERAT),
        bekreftet = true,
        fravaer = generateFravaersdata(),
        paakjenningstyper = setOf(PaakjenningsType.ALLERGENER, PaakjenningsType.TUNGE),
        paakjenningBeskrivelse = "Beskrivelse",
        sendtAv = "09876543210"
    )

    val fullValidRequest = KroniskSoknadRequest(
        virksomhetsnummer = validOrgNr,
        identitetsnummer = validIdentitetsnummer,
        arbeidstyper = setOf(ArbeidsType.KREVENDE, ArbeidsType.MODERAT, ArbeidsType.STILLESITTENDE),
        fravaer = generateFravaersdata(),
        paakjenningstyper = setOf(PaakjenningsType.ALLERGENER, PaakjenningsType.TUNGE, PaakjenningsType.ANNET, PaakjenningsType.GAAING, PaakjenningsType.HARDE, PaakjenningsType.REGELMESSIG, PaakjenningsType.STRESSENDE, PaakjenningsType.UKOMFORTABEL),
        paakjenningBeskrivelse = "Lorem Ipsum",
        bekreftet = true,
        dokumentasjon = null
    )

    val kroniskSoknadMedFil = fullValidRequest.copy(
        dokumentasjon = """
                data:image/pdf;base64,TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gQWxpcXVhbSB2aXRhZSBlcm9zIGEgZmVsaXMgbGFjaW5pYSBzb2xsaWNpdHVkaW4gdXQgZWdldCB0b3J0b3IuIFBoYXNlbGx1cyB2ZWhpY3VsYSBlZ2VzdGFzIG1hdHRpcy4gTnVuYyBldSBsaWJlcm8gdWxsYW1jb3JwZXIsIHBsYWNlcmF0IHNhcGllbiBlZ2V0LCBhY2N1bXNhbiBwdXJ1cy4gTWFlY2VuYXMgbWF4aW11cywgcHVydXMgbmVjIGxhY2luaWEgcHVsdmluYXIsIGR1aSBlbmltIGlhY3VsaXMgZGlhbSwgcXVpcyB2aXZlcnJhIG1hc3NhIGxpZ3VsYSBzaXQgYW1ldCBudWxsYS4gU2VkIG1heGltdXMgZXVpc21vZCBhbnRlIGluIHBvc3VlcmUuIFN1c3BlbmRpc3NlIGxpZ3VsYSB0ZWxsdXMsIGZpbmlidXMgdmVsIHBsYWNlcmF0IGlkLCBtYXhpbXVzIHNlZCBhbnRlLiBGdXNjZSBzaXQgYW1ldCBmZXJtZW50dW0gbWFnbmEuCgpDbGFzcyBhcHRlbnQgdGFjaXRpIHNvY2lvc3F1IGFkIGxpdG9yYSB0b3JxdWVudCBwZXIgY29udWJpYSBub3N0cmEsIHBlciBpbmNlcHRvcyBoaW1lbmFlb3MuIERvbmVjIGV1IHRvcnRvciBtYWxlc3VhZGEsIHVsbGFtY29ycGVyIG5pc2wgYXQsIHZ1bHB1dGF0ZSBlc3QuIFZpdmFtdXMgaWQgbG9yZW0gZWdlc3RhcyBhcmN1IHNvZGFsZXMgc2VtcGVyIHZpdGFlIHZlc3RpYnVsdW0gZG9sb3IuIENyYXMgZGFwaWJ1cywgZXJhdCBuZWMgZmF1Y2lidXMgZGFwaWJ1cywgZHVpIHZlbGl0IG9ybmFyZSB0ZWxsdXMsIHF1aXMgdWx0cmljaWVzIGxlbyB0ZWxsdXMgdXQgZXJhdC4gTWFlY2VuYXMgcG9ydGEgdGluY2lkdW50IHBsYWNlcmF0LiBDcmFzIGRpZ25pc3NpbSBsZWN0dXMgdGVsbHVzLCBldCBpbnRlcmR1bSByaXN1cyBwZWxsZW50ZXNxdWUgYXVjdG9yLiBJbiBtYXhpbXVzIGxhY2luaWEgbGVjdHVzLCBhIHNvZGFsZXMgbnVsbGEgdmFyaXVzIGdyYXZpZGEuIEV0aWFtIGhlbmRyZXJpdCBhdWd1ZSBvZGlvLCB2ZWwgcGhhcmV0cmEgb3JjaSBtYWxlc3VhZGEgbmVjLiBQZWxsZW50ZXNxdWUgaGFiaXRhbnQgbW9yYmkgdHJpc3RpcXVlIHNlbmVjdHVzIGV0IG5ldHVzIGV0IG1hbGVzdWFkYSBmYW1lcyBhYyB0dXJwaXMgZWdlc3Rhcy4gU2VkIGV0IGNvbmRpbWVudHVtIG9yY2ksIHZlbCBtYWxlc3VhZGEgbmVxdWUu
            """.trimIndent()
    )


    val kroniskKravRequestValid = KroniskKravRequest(
        virksomhetsnummer = validOrgNr,
        identitetsnummer = validIdentitetsnummer,
        perioder = setOf(Arbeidsgiverperiode(
            LocalDate.of(2020, 1, 5),
            LocalDate.of(2020, 1, 10),
            2,
            2590.8
        )),
        bekreftet = true,
        dokumentasjon = null,
        kontrollDager = null
    )

    val kroniskKravRequestMedFil = kroniskKravRequestValid.copy(
        dokumentasjon = """
                data:image/pdf;base64,TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gQWxpcXVhbSB2aXRhZSBlcm9zIGEgZmVsaXMgbGFjaW5pYSBzb2xsaWNpdHVkaW4gdXQgZWdldCB0b3J0b3IuIFBoYXNlbGx1cyB2ZWhpY3VsYSBlZ2VzdGFzIG1hdHRpcy4gTnVuYyBldSBsaWJlcm8gdWxsYW1jb3JwZXIsIHBsYWNlcmF0IHNhcGllbiBlZ2V0LCBhY2N1bXNhbiBwdXJ1cy4gTWFlY2VuYXMgbWF4aW11cywgcHVydXMgbmVjIGxhY2luaWEgcHVsdmluYXIsIGR1aSBlbmltIGlhY3VsaXMgZGlhbSwgcXVpcyB2aXZlcnJhIG1hc3NhIGxpZ3VsYSBzaXQgYW1ldCBudWxsYS4gU2VkIG1heGltdXMgZXVpc21vZCBhbnRlIGluIHBvc3VlcmUuIFN1c3BlbmRpc3NlIGxpZ3VsYSB0ZWxsdXMsIGZpbmlidXMgdmVsIHBsYWNlcmF0IGlkLCBtYXhpbXVzIHNlZCBhbnRlLiBGdXNjZSBzaXQgYW1ldCBmZXJtZW50dW0gbWFnbmEuCgpDbGFzcyBhcHRlbnQgdGFjaXRpIHNvY2lvc3F1IGFkIGxpdG9yYSB0b3JxdWVudCBwZXIgY29udWJpYSBub3N0cmEsIHBlciBpbmNlcHRvcyBoaW1lbmFlb3MuIERvbmVjIGV1IHRvcnRvciBtYWxlc3VhZGEsIHVsbGFtY29ycGVyIG5pc2wgYXQsIHZ1bHB1dGF0ZSBlc3QuIFZpdmFtdXMgaWQgbG9yZW0gZWdlc3RhcyBhcmN1IHNvZGFsZXMgc2VtcGVyIHZpdGFlIHZlc3RpYnVsdW0gZG9sb3IuIENyYXMgZGFwaWJ1cywgZXJhdCBuZWMgZmF1Y2lidXMgZGFwaWJ1cywgZHVpIHZlbGl0IG9ybmFyZSB0ZWxsdXMsIHF1aXMgdWx0cmljaWVzIGxlbyB0ZWxsdXMgdXQgZXJhdC4gTWFlY2VuYXMgcG9ydGEgdGluY2lkdW50IHBsYWNlcmF0LiBDcmFzIGRpZ25pc3NpbSBsZWN0dXMgdGVsbHVzLCBldCBpbnRlcmR1bSByaXN1cyBwZWxsZW50ZXNxdWUgYXVjdG9yLiBJbiBtYXhpbXVzIGxhY2luaWEgbGVjdHVzLCBhIHNvZGFsZXMgbnVsbGEgdmFyaXVzIGdyYXZpZGEuIEV0aWFtIGhlbmRyZXJpdCBhdWd1ZSBvZGlvLCB2ZWwgcGhhcmV0cmEgb3JjaSBtYWxlc3VhZGEgbmVjLiBQZWxsZW50ZXNxdWUgaGFiaXRhbnQgbW9yYmkgdHJpc3RpcXVlIHNlbmVjdHVzIGV0IG5ldHVzIGV0IG1hbGVzdWFkYSBmYW1lcyBhYyB0dXJwaXMgZWdlc3Rhcy4gU2VkIGV0IGNvbmRpbWVudHVtIG9yY2ksIHZlbCBtYWxlc3VhZGEgbmVxdWUu
            """.trimIndent()
    )

    val kroniskKrav = KroniskKrav(
        virksomhetsnummer = validOrgNr,
        identitetsnummer = validIdentitetsnummer,
        perioder = setOf(Arbeidsgiverperiode(
            LocalDate.of(2020, 1, 5),
            LocalDate.of(2020, 1, 10),
            5,
            2590.8
        )),
        sendtAv = validIdentitetsnummer,
        kontrollDager = null
    )
    
}

private fun generateFravaersdata() = (0..24L)
    .map { FravaerData(LocalDate.now().minusMonths(it).toYearMonthString(), Random.nextInt(0, 28)) }
    .toMutableSet()

fun LocalDate.toYearMonthString() = this.format(DateTimeFormatter.ofPattern("yyyy-MM"))