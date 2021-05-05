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

        validerArbeidsforhold(listOf(arbeidsForhold1, arbeidsForhold2), TestData.gyldigSoeknad)

        assertThrows<ConstraintViolationException> {
            validerArbeidsforhold(listOf(arbeidsForhold2), TestData.gyldigSoeknad)
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