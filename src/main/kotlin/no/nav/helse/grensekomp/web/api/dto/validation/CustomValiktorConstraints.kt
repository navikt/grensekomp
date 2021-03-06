package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import no.nav.helse.grensekomp.service.RefusjonskravService
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import no.nav.helse.grensekomp.web.dto.validation.BostedlandValidator
import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator
import no.nav.helse.grensekomp.web.dto.validation.OrganisasjonsnummerValidator
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation
import org.valiktor.Validator
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isGreaterThanOrEqualTo
import java.time.LocalDate
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

class BostedslandConstraints : CustomConstraint

fun <E> Validator<E>.Property<String?>.isValidBostedsland() =
    this.validate(BostedslandConstraints()) { BostedlandValidator.isValid(it)}

class OverloependePerioderConstraints : CustomConstraint
fun validerKravPerioden(refusjonskrav: RefusjonskravDto, refusjonskravService: RefusjonskravService) {
    val refKrav = refusjonskravService.getPersonKrav(refusjonskrav.identitetsnummer)
    refKrav
        .filter { it.status != RefusjonskravStatus.SLETTET && it.virksomhetsnummer == refusjonskrav.virksomhetsnummer }
        .forEach { it ->
        if(refusjonskrav.periode.overlap(it.periode)) {
            throw ConstraintViolationException(
                setOf(
                    DefaultConstraintViolation(
                        "periode.fom",
                        constraint = OverloependePerioderConstraints(),
                        value = refusjonskrav.identitetsnummer
                    )
                )
            )
        }
    }
}

class InnenforFristenConstraint : CustomConstraint
fun <E> Validator<E>.Property<LocalDate?>.validerInnenforFristen(frist: LocalDate) =
    this.validate(InnenforFristenConstraint()) { fom ->
        return@validate fom!!.isAfter(frist) || fom!!.isEqual(frist)
    }


