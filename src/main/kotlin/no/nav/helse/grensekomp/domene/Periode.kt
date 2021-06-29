package no.nav.helse.grensekomp.domene

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val beregnetMånedsinntekt: Double
) : Comparable<Periode> {

    constructor(fom: LocalDate,tom: LocalDate, beregnetMånedsinntekt: Int) : this(fom, tom, beregnetMånedsinntekt.toDouble())

    companion object {
        val refusjonFraDato = LocalDate.of(2021, 1, 29)
        val weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val justeringsFaktor = 0.7
        //val minFraDato = LocalDate.now().minusMonths(6).withDayOfMonth(1)
        // midlertidig frist etter 29 juli
        val minFraDato = LocalDate.of(2021, 3,1)
    }

    override fun compareTo(other: Periode): Int {
        if (other.fom.isAfter(fom))
            return -1
        if (other.fom.isBefore(fom))
            return 1
        return 0
    }

    fun estimertUtbetaling(seksG: Double): Int {
        val antallUkedager = fom.datesUntil(tom.plusDays(1))
            .filter { d -> !weekend.contains(d.dayOfWeek) }
            .count().toInt()

        val dagsats = (min(beregnetMånedsinntekt * 12, seksG) / 260).roundToInt()
        val nedjustertDagsats = (dagsats * justeringsFaktor).roundToInt()
        return nedjustertDagsats * antallUkedager
    }

    //https://stackoverflow.com/questions/325933/determine-whether-two-date-ranges-overlap
    //DateRangesOverlap = max(start1, start2) < min(end1, end2)
    fun overlap(other: Periode) : Boolean {
        return maxFom(other.fom) < minTom(other.tom)
    }

    private fun maxFom(otherFom : LocalDate) : LocalDate{
        return if (otherFom.isAfter(fom))
            otherFom
        else fom
    }

    private fun minTom(otherTom : LocalDate) : LocalDate{
        return if (otherTom.isAfter(tom))
            tom
        else otherTom
    }
}
