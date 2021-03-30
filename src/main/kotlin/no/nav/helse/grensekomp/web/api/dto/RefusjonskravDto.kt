package no.nav.helse.grensekomp.web.api.dto

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Periode.Companion.refusjonFraDato
import no.nav.helse.grensekomp.web.api.dto.validation.*
import org.valiktor.functions.*
import org.valiktor.validate
import java.time.LocalDate

data class RefusjonskravDto(
        val identitetsnummer: String,
        val virksomhetsnummer: String,
        val periode: Periode,
        val bekreftet: Boolean,
        val bostedsland: String
) {

    init {
        val obj = this
        validate(this) {
            validate(RefusjonskravDto::identitetsnummer).isValidIdentitetsnummer()
            validate(RefusjonskravDto::virksomhetsnummer).isValidOrganisasjonsnummer()
            validate(RefusjonskravDto::bekreftet).isTrue()

            validate(RefusjonskravDto::periode).validate {
                validate(Periode::beloep).isPositive()
                validate(Periode::beloep).isLessThanOrEqualTo(1_000_000.0)
                validate(Periode::antallDagerMedRefusjon).isPositiveOrZero()
                validate(Periode::tom).isGreaterThanOrEqualTo(it.fom)
                validate(Periode::tom).isLessThanOrEqualTo(LocalDate.now())

                // antall refusjonsdager kan ikke være lenger enn periodens lengde
                validate(Periode::antallDagerMedRefusjon).refusjonsDagerIkkeOverstigerPeriodelengder(obj.periode.fom, obj.periode.tom)
                // kan ikke kreve refusjon for dager før første refusjonsdato
                validate(Periode::fom).isGreaterThanOrEqualTo(refusjonFraDato)
                // tom periode kan ikke ha refusjonsbeløp
                validate(Periode::beloep).tomPeriodeKanIkkeHaBeloepConstraint(obj.periode.antallDagerMedRefusjon)
            }
            validate(RefusjonskravDto::bostedsland).isNotEmpty()
            validate(RefusjonskravDto::bostedsland).hasSize(3,3)
        }
    }
}
