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
            0
        )

        validationShouldFailFor(Periode::fom) {
            validate(periode) {
                validate(Periode::fom).validerInnenforFristen()
            }
        }
    }

    @Test
    fun `Skal godta dato i starten av måneden`() {

        val periode = Periode(
            LocalDate.now().withDayOfMonth(1).minusMonths(6),
            LocalDate.MAX,
            0
        )
        validate(periode) {
            validate(Periode::fom).validerInnenforFristen()
        }

    }
    @Test
    fun `Skal godta dato for 2 av måneder siden`() {

        val periode = Periode(
            LocalDate.now().withDayOfMonth(1).minusMonths(2),
            LocalDate.MAX,
            0
        )
        validate(periode) {
            validate(Periode::fom).validerInnenforFristen()
        }

    }

    @Test
    fun `Skal ikke godta dato i slutten av måneden for 7 måneder siden`() {

        val periode = Periode(
            LocalDate.now().minusMonths(6).withDayOfMonth(1).minusDays(1),
            LocalDate.MAX,
            0
        )

        validationShouldFailFor(Periode::fom) {
            validate(periode) {
                validate(Periode::fom).validerInnenforFristen()
            }
        }

    }

}