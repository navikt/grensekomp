package no.nav.helse.grensekomp

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import java.time.LocalDate.of

object TestData {
    val validIdentitetsnummer = "20015001543"
    val notValidIdentitetsnummer = "50012001987"
    val validOrgNr = "123456785"
    val notValidOrgNr = "123456789"
    val opprettetAv = "20015001543"
    val gyldigKrav = Refusjonskrav(
            opprettetAv,
            validIdentitetsnummer,
            validOrgNr,
            Periode(of(2020, 4,4), of(2020, 4,10), 1000),
            true,
        "SWE",
            RefusjonskravStatus.MOTTATT
    )
}