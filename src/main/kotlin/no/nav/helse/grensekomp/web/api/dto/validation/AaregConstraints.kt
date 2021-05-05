package no.nav.helse.grensekomp.web.api.dto.validation

import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.Arbeidsforhold
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.metrics.MANGLENDE_ARBEIDSFORHOLD
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import java.time.LocalDate
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.Periode as AaregPeriode


class ArbeidsforholdConstraint : CustomConstraint
class ArbeidsforholdStartConstraint : CustomConstraint
class ArbeidsforholdLengdeConstraint : CustomConstraint
val MAKS_DAGER_OPPHOLD = 3L

@KtorExperimentalAPI
fun validerArbeidsforhold(aktuelleArbeidsforhold: List<Arbeidsforhold>, refusjonskrav: RefusjonskravDto) {
    val logger = LoggerFactory.getLogger("AAREG-validator")

    val sisteArbeidsforhold = aktuelleArbeidsforhold
        .sortedBy { it.ansettelsesperiode.periode.tom ?: LocalDate.MAX }
        .takeLast(2)

    logger.info(sisteArbeidsforhold.toString())

    val oppholdMellomPerioderOverstigerMaks = oppholdMellomPerioderOverstigerDager(sisteArbeidsforhold, MAKS_DAGER_OPPHOLD)
    val ansPeriode = if (sisteArbeidsforhold.size <= 1 || oppholdMellomPerioderOverstigerMaks)
        sisteArbeidsforhold.lastOrNull()?.ansettelsesperiode?.periode ?: AaregPeriode(LocalDate.MAX, LocalDate.MAX)
    else
        AaregPeriode(
            sisteArbeidsforhold.first().ansettelsesperiode.periode.fom,
            sisteArbeidsforhold.last().ansettelsesperiode.periode.tom
        )

    val kravPeriodeSubsettAvAnsPeriode = ansPeriode.tom == null ||
            refusjonskrav.periode.tom.isBefore(ansPeriode.tom) ||
            refusjonskrav.periode.tom == ansPeriode.tom

    if (aktuelleArbeidsforhold == null || !kravPeriodeSubsettAvAnsPeriode) {
        MANGLENDE_ARBEIDSFORHOLD.labels("arbeidsforhold_mangler").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "identitetsnummer",
                    constraint = ArbeidsforholdConstraint(),
                    value = refusjonskrav.virksomhetsnummer
                )
            )
        )
    }

    val arbeidsforholdGammeltNok = ansPeriode.fom?.isBefore(Periode.refusjonFraDato.plusDays(1)) ?: false
    if (!arbeidsforholdGammeltNok) {
        MANGLENDE_ARBEIDSFORHOLD.labels("arbeidsforhold_foer_29_jan").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "fom",
                    constraint = ArbeidsforholdStartConstraint(),
                    value = refusjonskrav.virksomhetsnummer
                )
            )
        )
    }

    // B: må endres til et flagg på modellen eller droppes
//    val arbeidsforholdVart4Uker = ansPeriode.fom!!.isBefore(refusjonskrav.periode.fom.minusDays(28))
//    if (!arbeidsforholdVart4Uker) {
//        MANGLENDE_ARBEIDSFORHOLD.labels("yngre_enn_4_uker").inc()
//        throw ConstraintViolationException(
//            setOf(
//                DefaultConstraintViolation(
//                    "identitetsnummer",
//                    constraint = ArbeidsforholdLengdeConstraint(),
//                    value = refusjonskrav.virksomhetsnummer
//                )
//            )
//        )
//    }
}

fun oppholdMellomPerioderOverstigerDager(sisteArbeidsforhold: List<Arbeidsforhold>, dager: Long): Boolean {
    return sisteArbeidsforhold.first().ansettelsesperiode.periode.tom?.plusDays(dager)
        ?.isBefore(sisteArbeidsforhold.last().ansettelsesperiode.periode.fom) ?: true
}
