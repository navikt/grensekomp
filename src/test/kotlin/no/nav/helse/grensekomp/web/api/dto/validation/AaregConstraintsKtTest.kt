package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.arbeidsgiver.integrasjoner.aareg.*
import no.nav.helse.grensekomp.TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.valiktor.ConstraintViolationException
import java.time.LocalDate
import java.time.LocalDate.of
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
            Ansettelsesperiode(
                Periode(
                    of(2019, 3, 1),
                    of(2021, 4, 30)
                )
            ),
            LocalDateTime.now()
        )
        val arbeidsForhold2 = Arbeidsforhold(
            arbeidsgiver,
            opplysningspliktig,
            emptyList(),
            Ansettelsesperiode(
                Periode(
                    of(2021, 5, 1),
                    null
                )
            ),
            LocalDateTime.now()
        )

        validerArbeidsforhold(
            listOf(arbeidsForhold1, arbeidsForhold2),
            TestData.gyldigSoeknad.copy(periode = TestData.gyldigSoeknad.periode.copy(tom = of(2021, 7, 1)))
        )

        assertThrows<ConstraintViolationException> {
            validerArbeidsforhold(listOf(arbeidsForhold2), TestData.gyldigSoeknad)
        }

        assertThrows<ConstraintViolationException> {
            validerArbeidsforhold(emptyList(), TestData.gyldigSoeknad)
        }

        assertThrows<ConstraintViolationException> {
            val forLangtOpphold = arbeidsForhold2.copy(
                ansettelsesperiode = Ansettelsesperiode(
                    Periode(
                        LocalDate.of(2021, 5, 4),
                        null
                    )
                )
            )
            validerArbeidsforhold(listOf(arbeidsForhold1, forLangtOpphold), TestData.gyldigSoeknad)
        }

    }

    @Test
    fun `merge fragmented periods`() {
        assertThat(slåSammenPerioder(listOf(
            // skal ble merget til 1 periode fra 1.1.21 til 28.2.21
            Periode(of(2021, 1, 1 ), of(2021, 1, 29)),
            Periode(of(2021, 2,  1), of(2021, 2, 13)),
            Periode(of(2021, 2, 15 ), of(2021, 2, 28)),

            // skal bli merget til 1
            Periode(of(2021, 3, 20 ), of(2021, 3, 31)),
            Periode(of(2021, 4, 2 ), of(2021, 4, 30)),

            // skal bli merget til 1
            Periode(of(2021, 7, 1 ), of(2021, 8, 30)),
            Periode(of(2021, 9, 1 ), null),
        ))).hasSize(3)

        assertThat(slåSammenPerioder(listOf(
            Periode(of(2021, 1, 1 ), of(2021, 1, 29)),
            Periode(of(2021, 9, 1 ), null),
        ))).hasSize(2)

        assertThat(slåSammenPerioder(listOf(
            Periode(of(2021, 9, 1 ), null),
        ))).hasSize(1)

    }
}