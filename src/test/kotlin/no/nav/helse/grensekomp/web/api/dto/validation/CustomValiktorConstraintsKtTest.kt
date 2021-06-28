package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import no.nav.helse.grensekomp.web.api.resreq.validationShouldFailFor
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.valiktor.validate
import java.time.LocalDate

class CustomValiktorConstraintsKtTest {

    @Test
    fun `Skal feile for minst mulig dato`() {
        val periode = Periode(
            LocalDate.MIN,
            LocalDate.MAX,
            0)

        validationShouldFailFor(Periode::fom) {
            validate(periode) {
                validate(Periode::fom).validerInnenforFristen()
            }
        }
    }

}