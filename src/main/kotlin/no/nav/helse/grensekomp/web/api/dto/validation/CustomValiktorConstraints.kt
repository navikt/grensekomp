package no.nav.helse.grensekomp.web.api.dto.validation

import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.metrics.MANGLENDE_ARBEIDSFORHOLD
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import no.nav.helse.grensekomp.web.dto.validation.BostedlandValidator
import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator
import no.nav.helse.grensekomp.web.dto.validation.OrganisasjonsnummerValidator
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import org.valiktor.Validator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

interface CustomConstraint : Constraint {
    override val messageBundle: String
        get() = "validation/validation-messages"
}

class IdentitetsnummerConstraint : CustomConstraint
fun <E> Validator<E>.Property<String?>.isValidIdentitetsnummer() =
    this.validate(IdentitetsnummerConstraint()) { FoedselsNrValidator.isValid(it) }


class OrganisasjonsnummerConstraint : CustomConstraint

fun <E> Validator<E>.Property<String?>.isValidOrganisasjonsnummer() =
    this.validate(OrganisasjonsnummerConstraint()) { OrganisasjonsnummerValidator.isValid(it) }

class RefusjonsdagerKanIkkeOverstigePeriodelengdenConstraint : CustomConstraint

fun <E> Validator<E>.Property<Int?>.refusjonsDagerIkkeOverstigerPeriodelengder(fom: LocalDate, tom: LocalDate) =
    this.validate(RefusjonsdagerKanIkkeOverstigePeriodelengdenConstraint()) { antallDagerMedRefusjon ->
        !(antallDagerMedRefusjon != null && ChronoUnit.DAYS.between(fom, tom.plusDays(1)) < antallDagerMedRefusjon)
    }

class TomPeriodeKanIkkeHaBeloepConstraint : CustomConstraint

fun <E> Validator<E>.Property<Double?>.tomPeriodeKanIkkeHaBeloepConstraint(antallDagerMedRefusjon: Int) =
    this.validate(TomPeriodeKanIkkeHaBeloepConstraint()) { beloep ->
        !(beloep != null && antallDagerMedRefusjon == 0 && beloep > 0)
    }


class BostedslandConstraints : CustomConstraint

fun <E> Validator<E>.Property<String?>.isValidBostedsland() =
    this.validate(BostedslandConstraints()) { BostedlandValidator.isValid(it)}
