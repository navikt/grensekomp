package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.arbeidsgiver.integrasjoner.aareg.*
import no.nav.helse.grensekomp.TestData
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.valiktor.ConstraintViolationException
import java.time.LocalDate
import java.time.LocalDateTime

internal class AaregConstraintsKtTest {

    @Test
    fun `Kant-i-kant arbeidsforhold teller som sammenhengende`() {

        /*

        val aforhold = listOf(
            [Arbeidsforhold(arbeidsgiver=Arbeidsgiver(type="Organisasjon", organisasjonsnummer="810007842"), opplysningspliktig=Opplysningspliktig(type="Organisasjon", organisasjonsnummer="810007672"),
                arbeidsavtaler=[Arbeidsavtale(stillingsprosent=100.0, gyldighetsperiode=Periode(fom=2019-01-01, tom=null))], ansettelsesperiode=Ansettelsesperiode(periode=Periode(fom=2019-01-01, tom=2021-02-28)), registrert=2021-05-04T23:33:36.807),
        Arbeidsforhold(arbeidsgiver=Arbeidsgiver(type=Organisasjon, organisasjonsnummer=810007842), opplysningspliktig=Opplysningspliktig(type=Organisasjon, organisasjonsnummer=810007672),
            arbeidsavtaler=[Arbeidsavtale(stillingsprosent=100.0, gyldighetsperiode=Periode(fom=2021-03-01, tom=null))], ansettelsesperiode=Ansettelsesperiode(periode=Periode(fom=2021-03-01, tom=null)), registrert=2021-05-04T23:33:36.990)]
        )
        */

        val arbeidsgiver = Arbeidsgiver("AS", "1232242423")
        val opplysningspliktig = Opplysningspliktig("AS", "1212121212")
        val arbeidsForhold1 = Arbeidsforhold(
            arbeidsgiver,
            opplysningspliktig,
            emptyList(),
            Ansettelsesperiode(Periode(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2021, 2, 28)
            )),
            LocalDateTime.now()
        )
        val arbeidsForhold2 = Arbeidsforhold(
            arbeidsgiver,
            opplysningspliktig,
            emptyList(),
            Ansettelsesperiode(Periode(
                LocalDate.of(2021, 3, 1),
                null
            )),
            LocalDateTime.now()
        )

        validerArbeidsforhold(listOf(arbeidsForhold1, arbeidsForhold2), TestData.gyldigSoeknad.copy(periode = TestData.gyldigSoeknad.periode.copy(tom = LocalDate.of(2021, 5, 1))))

        assertThrows<ConstraintViolationException> {
            validerArbeidsforhold(listOf(arbeidsForhold2), TestData.gyldigSoeknad)
        }

        assertThrows<ConstraintViolationException> {
            validerArbeidsforhold(emptyList(), TestData.gyldigSoeknad)
        }

        assertThrows<ConstraintViolationException> {
            val forLangtOpphold = arbeidsForhold2.copy(ansettelsesperiode = Ansettelsesperiode(Periode(
                LocalDate.of(2021, 3, 4),
                null
            )))
            validerArbeidsforhold(listOf(arbeidsForhold1, forLangtOpphold), TestData.gyldigSoeknad)
        }

    }
}