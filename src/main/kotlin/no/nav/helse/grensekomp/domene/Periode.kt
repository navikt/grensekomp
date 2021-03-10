package no.nav.helse.grensekomp.domene

import java.time.LocalDate

data class Periode(
        val fom: LocalDate,
        val tom: LocalDate,
        val antallDagerMedRefusjon: Int,
        val beloep: Double
): Comparable<Periode> {
    companion object {
        val refusjonFraDato = LocalDate.of(2020, 3, 16)
        val maksOppholdMellomPerioder = 16
        val maksimalAGPLengde = 16
        val arbeidsgiverBetalerForDager = 3
    }

    override fun compareTo(other: Periode): Int {
        if(other.fom.isAfter(fom))
            return -1
        if(other.fom.isBefore(fom))
            return 1
        return 0
    }
}
