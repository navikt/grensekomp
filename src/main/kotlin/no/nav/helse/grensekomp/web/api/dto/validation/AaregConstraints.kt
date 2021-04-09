package no.nav.helse.grensekomp.web.api.dto.validation

import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.metrics.MANGLENDE_ARBEIDSFORHOLD
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import java.time.LocalDate
import java.util.*


class ArbeidsforholdConstraint : CustomConstraint
class ArbeidsforholdStartConstraint : CustomConstraint
class ArbeidsforholdLengdeConstraint : CustomConstraint

@KtorExperimentalAPI
suspend fun validerArbeidsforhold(aaregClient: AaregArbeidsforholdClient, refusjonskrav: RefusjonskravDto) {
    val aktueltArbeidsforhold = aaregClient.hentArbeidsforhold(refusjonskrav.identitetsnummer, UUID.randomUUID().toString())
        .filter { it.arbeidsgiver.organisasjonsnummer == refusjonskrav.virksomhetsnummer }
        .sortedBy { it.ansettelsesperiode.periode.tom ?: LocalDate.MAX }
        .lastOrNull()


    val ansPeriode = aktueltArbeidsforhold?.ansettelsesperiode?.periode ?:
    no.nav.helse.arbeidsgiver.integrasjoner.aareg.Periode(LocalDate.MAX, LocalDate.MAX)

    val kravPeriodeSubsettAvAnsPeriode = ansPeriode.tom == null ||
            refusjonskrav.periode.tom.isBefore(ansPeriode.tom) ||
            refusjonskrav.periode.tom == ansPeriode.tom

    if (aktueltArbeidsforhold == null || !kravPeriodeSubsettAvAnsPeriode) {
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

    val arbeidsforholdVart4Uker = ansPeriode.fom!!.isBefore(refusjonskrav.periode.fom.minusDays(28))
    if (!arbeidsforholdVart4Uker) {
        MANGLENDE_ARBEIDSFORHOLD.labels("yngre_enn_4_uker").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "identitetsnummer",
                    constraint = ArbeidsforholdLengdeConstraint(),
                    value = refusjonskrav.virksomhetsnummer
                )
            )
        )
    }

}
