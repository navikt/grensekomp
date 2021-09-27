package no.nav.helse.grensekomp

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import java.time.LocalDate
import java.time.LocalDate.of

object TestData {
   val validIdentitetsnummer = "20015001543"
    val notValidIdentitetsnummer = "50012001987"
    val validOrgNr = "910098896"
    val notValidOrgNr = "123456789"
    val opprettetAv = "20015001543"
    val gyldigKrav = Refusjonskrav(
            opprettetAv,
            validIdentitetsnummer,
            validOrgNr,
            Periode(of(2021, 3,30),of(2021, 4,5),1000.0),
            true,
        "SWE",
            RefusjonskravStatus.MOTTATT
    )
    val gyldigSoeknad = RefusjonskravDto(
        identitetsnummer = validIdentitetsnummer,
        virksomhetsnummer = validOrgNr,
        periode = Periode(
            of(2021, 3,30),
            of(2021, 6,5),
            1000.0),
        bekreftet = true,
        bostedsland = "SWE")

    val fristDato = LocalDate.of(2021, 2, 1)
}