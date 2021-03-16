package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator
import no.nav.helse.grensekomp.web.dto.validation.OrganisasjonsnummerValidator
import org.valiktor.Constraint
import org.valiktor.Validator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

interface CustomConstraint : Constraint {
    override val messageBundle: String
        get() = "validation/validation-messages"
}


class ArbeidsforholdConstraint : CustomConstraint

class IdentitetsnummerConstraint : CustomConstraint

fun <E> Validator<E>.Property<String?>.isValidIdentitetsnummer() =
    this.validate(IdentitetsnummerConstraint()) { FoedselsNrValidator.isValid(it) }


class OrganisasjonsnummerConstraint : CustomConstraint

fun <E> Validator<E>.Property<String?>.isValidOrganisasjonsnummer() =
    this.validate(OrganisasjonsnummerConstraint()) { OrganisasjonsnummerValidator.isValid(it) }

class RefusjonsdagerKanIkkeOverstigePeriodelengdenConstraint : CustomConstraint

fun <E> Validator<E>.Property<Int?>.refusjonsDagerIkkeOverstigerPeriodelengder(fom: LocalDate, tom: LocalDate) =
    this.validate(RefusjonsdagerKanIkkeOverstigePeriodelengdenConstraint()) { antallDagerMedRefusjon ->
        antallDagerMedRefusjon != null && ChronoUnit.DAYS.between(fom, tom.plusDays(1)) < antallDagerMedRefusjon
    }

class TomPeriodeKanIkkeHaBeloepConstraint : CustomConstraint

fun <E> Validator<E>.Property<Double?>.tomPeriodeKanIkkeHaBeloepConstraint(antallDagerMedRefusjon: Int) =
    this.validate(TomPeriodeKanIkkeHaBeloepConstraint()) { beloep ->
        beloep != null && antallDagerMedRefusjon == 0 && beloep > 0
    }

class RefusjonsdagerInnenforGyldigPeriodeConstraint : CustomConstraint

fun <E> Validator<E>.Property<Periode?>.refusjonsdagerInnenforGyldigPeriode(refusjonsdagerFom: LocalDate) =
    this.validate(RefusjonsdagerInnenforGyldigPeriodeConstraint()) { p ->
        val validDays = ChronoUnit.DAYS.between(refusjonsdagerFom, p?.tom?.plusDays(1))
        p != null && (p.fom >= refusjonsdagerFom || (p.antallDagerMedRefusjon == 0 || p.antallDagerMedRefusjon <= validDays))
    }
