package no.nav.helse.grensekomp.domene

import java.time.LocalDate

data class Periode(
        val fom: LocalDate,
        val tom: LocalDate,
        val antallDagerMedRefusjon: Int,
        val dagsats: Double
): Comparable<Periode> {
    companion object {
        val refusjonFraDato = LocalDate.of(2021, 1, 29)
    }

    override fun compareTo(other: Periode): Int {
        if(other.fom.isAfter(fom))
            return -1
        if(other.fom.isBefore(fom))
            return 1
        return 0
    }
}
