package no.nav.helse.grensekomp.web.api.dto.validation

import no.nav.helse.grensekomp.web.dto.validation.BostedlandValidator
import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator
import no.nav.helse.grensekomp.web.dto.validation.OrganisasjonsnummerValidator
import org.valiktor.Constraint
import org.valiktor.Validator

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
