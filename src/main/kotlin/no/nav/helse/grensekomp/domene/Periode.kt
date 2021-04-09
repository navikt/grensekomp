package no.nav.helse.grensekomp.domene

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.math.min

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val beregnetMånedsinntekt: Int
) : Comparable<Periode> {
    companion object {
        val refusjonFraDato = LocalDate.of(2021, 1, 29)
        val weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val justeringsFaktor = 0.7
    }

    override fun compareTo(other: Periode): Int {
        if (other.fom.isAfter(fom))
            return -1
        if (other.fom.isBefore(fom))
            return 1
        return 0
    }

    fun estimertUtbetaling(seksG: Int): Double {
        val antallUkedager = fom.datesUntil(tom.plusDays(1))
            .filter { d -> !weekend.contains(d.dayOfWeek) }
            .count()

        return min(beregnetMånedsinntekt * 12, seksG) / 260 * antallUkedager * justeringsFaktor
    }
}
