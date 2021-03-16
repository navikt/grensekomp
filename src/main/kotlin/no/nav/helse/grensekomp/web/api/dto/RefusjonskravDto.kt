package no.nav.helse.grensekomp.web.api.dto

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Periode.Companion.refusjonFraDato
import no.nav.helse.grensekomp.web.dto.validation.*
import org.valiktor.functions.*
import org.valiktor.validate
import java.time.LocalDate

data class RefusjonskravDto(
        val identitetsnummer: String,
        val virksomhetsnummer: String,
        val periode: Periode
) {

    init {
        validate(this) {
            validate(RefusjonskravDto::identitetsnummer).isValidIdentitetsnummer()
            validate(RefusjonskravDto::virksomhetsnummer).isValidOrganisasjonsnummer()

            validate(RefusjonskravDto::periode).validate {
                validate(Periode::beloep).isPositive()
                validate(Periode::beloep).isLessThanOrEqualTo(1_000_000.0)
                validate(Periode::antallDagerMedRefusjon).isPositiveOrZero()
                validate(Periode::tom).isGreaterThanOrEqualTo(it.fom)
                validate(Periode::tom).isLessThanOrEqualTo(LocalDate.now())
            }

            // antall refusjonsdager kan ikke være lenger enn periodens lengde
            validate(RefusjonskravDto::periode).refujonsDagerIkkeOverstigerPeriodelengder()

            // kan ikke kreve refusjon for dager før første refusjonsdato
            validate(RefusjonskravDto::periode).refusjonsdagerInnenforGyldigPeriode(refusjonFraDato)

            // tom periode kan ikke ha refusjonsbeløp
            validate(RefusjonskravDto::periode).tomPeriodeKanIkkeHaBeloepConstraint()
        }
    }
}
