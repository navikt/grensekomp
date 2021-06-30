package no.nav.helse.grensekomp.web.api.dto.validation

import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.Arbeidsforhold
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.metrics.MANGLENDE_ARBEIDSFORHOLD
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.Periode as AaregPeriode


class ArbeidsforholdConstraint : CustomConstraint
class ArbeidsforholdStartConstraint : CustomConstraint
class ArbeidsforholdLengdeConstraint : CustomConstraint

val MAKS_DAGER_OPPHOLD = 3L

@KtorExperimentalAPI
fun validerArbeidsforhold(aktuelleArbeidsforhold: List<Arbeidsforhold>, krav: RefusjonskravDto) {
    val ansattPerioder = slåSammenPerioder(aktuelleArbeidsforhold.map { it.ansettelsesperiode.periode })

    val kravPeriodeSubsettAvAnsPeriode = ansattPerioder.any { ansPeriode ->
        (ansPeriode.tom == null || krav.periode.tom.isBefore(ansPeriode.tom) || krav.periode.tom == ansPeriode.tom)
                && ansPeriode.fom!!.isBefore(krav.periode.fom)
    }

    if (aktuelleArbeidsforhold == null || !kravPeriodeSubsettAvAnsPeriode) {
        MANGLENDE_ARBEIDSFORHOLD.labels("arbeidsforhold_mangler").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "identitetsnummer",
                    constraint = ArbeidsforholdConstraint(),
                    value = krav.virksomhetsnummer
                )
            )
        )
    }

    val arbeidsforholdGammeltNok = ansattPerioder.any { ansPeriode -> ansPeriode.fom?.isBefore(Periode.refusjonFraDato.plusDays(1)) ?: false}
    if (!arbeidsforholdGammeltNok) {
        MANGLENDE_ARBEIDSFORHOLD.labels("arbeidsforhold_foer_29_jan").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "fom",
                    constraint = ArbeidsforholdStartConstraint(),
                    value = krav.virksomhetsnummer
                )
            )
        )
    }
}

fun slåSammenPerioder(list: List<AaregPeriode>): List<AaregPeriode> {
    if (list.size < 2) return list

    val remainingPeriods = list
        .sortedBy { it.fom }
        .toMutableList()

    val merged = ArrayList<AaregPeriode>()

    do {
        var currentPeriod = remainingPeriods[0]
        remainingPeriods.removeAt(0)

        do {
            val connectedPeriod = remainingPeriods
                .find { !oppholdMellomPerioderOverstigerDager(currentPeriod, it, MAKS_DAGER_OPPHOLD)}
            if (connectedPeriod != null) {
                currentPeriod = AaregPeriode(currentPeriod.fom, connectedPeriod.tom)
                remainingPeriods.remove(connectedPeriod)
            }
        } while(connectedPeriod != null)

        merged.add(currentPeriod)
    } while (remainingPeriods.isNotEmpty())

    return merged
}

fun oppholdMellomPerioderOverstigerDager(sisteArbeidsforhold: List<Arbeidsforhold>, dager: Long): Boolean {
    return sisteArbeidsforhold.first().ansettelsesperiode.periode.tom?.plusDays(dager)
        ?.isBefore(sisteArbeidsforhold.last().ansettelsesperiode.periode.fom) ?: true
}

fun oppholdMellomPerioderOverstigerDager(
    a1: AaregPeriode,
    a2: AaregPeriode,
    dager: Long
): Boolean {
    return a1.tom?.plusDays(dager)?.isBefore(a2.fom) ?: true
}
