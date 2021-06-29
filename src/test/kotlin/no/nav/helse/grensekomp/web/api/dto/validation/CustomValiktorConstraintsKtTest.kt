package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.web.api.resreq.validationShouldFailFor
import org.junit.jupiter.api.Test
import no.nav.helse.grensekomp.TestData.fristDato

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
                validate(Periode::fom).validerInnenforFristen(fristDato)
            }
        }
    }

    @Test
    fun `Skal godta dato i starten av måneden`() {

        val periode = Periode(
            LocalDate.of(2021, 2, 1),
            LocalDate.MAX,
            0
        )
        validate(periode) {
            validate(Periode::fom).validerInnenforFristen(fristDato)
        }

    }
    @Test
    fun `Skal godta dato 2 måneder før fristen`() {

        val periode = Periode(
            LocalDate.of(2021, 4, 1),
            LocalDate.MAX,
            0
        )
        validate(periode) {
            validate(Periode::fom).validerInnenforFristen(fristDato)
        }

    }

    @Test
    fun `Skal ikke godta dato dagen før fristen`() {

        val periode = Periode(
            LocalDate.of(2021, 1, 31),
            LocalDate.MAX,
            0
        )

        validationShouldFailFor(Periode::fom) {
            validate(periode) {
                validate(Periode::fom).validerInnenforFristen(fristDato)
            }
        }

    }

}