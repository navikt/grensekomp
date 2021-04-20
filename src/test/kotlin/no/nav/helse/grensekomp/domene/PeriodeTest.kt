package no.nav.helse.grensekomp.domene

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

internal class PeriodeTest {

    @Test
    fun estimertUtbetaling() {

        val estimertRefusjon = Periode(
            LocalDate.of(2021, 2,1),
            LocalDate.of(2021, 2,28),
            25000
        ).estimertUtbetaling(101351 * 6.0)

        assertThat(estimertRefusjon).isEqualTo(16160)
    }
}