package no.nav.helse.grensekomp.web.dto

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Periode.Companion.arbeidsgiverBetalerForDager
import no.nav.helse.grensekomp.domene.Periode.Companion.maksOppholdMellomPerioder
import no.nav.helse.grensekomp.domene.Periode.Companion.maksimalAGPLengde
import no.nav.helse.grensekomp.domene.Periode.Companion.refusjonFraDato
import no.nav.helse.grensekomp.web.dto.validation.*
import org.valiktor.functions.isGreaterThanOrEqualTo
import org.valiktor.functions.isLessThanOrEqualTo
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.functions.validateForEach
import org.valiktor.validate
import java.time.LocalDate

data class RefusjonskravDto(
        val identitetsnummer: String,
        val virksomhetsnummer: String,
        val perioder: Set<Periode>
) {

    init {
        validate(this) {
            validate(RefusjonskravDto::identitetsnummer).isValidIdentitetsnummer()
            validate(RefusjonskravDto::virksomhetsnummer).isValidOrganisasjonsnummer()

            validate(RefusjonskravDto::perioder).validateForEach {
                validate(Periode::beloep).isPositiveOrZero()
                validate(Periode::beloep).isLessThanOrEqualTo(1_000_000.0)
                validate(Periode::antallDagerMedRefusjon).isPositiveOrZero()
            }

            validate(RefusjonskravDto::perioder).validateForEach {
                validate(Periode::tom).isGreaterThanOrEqualTo(it.fom)
                validate(Periode::tom).isLessThanOrEqualTo(LocalDate.now())
            }


            // antall refusjonsdager kan ikke være lenger enn periodens lengde
            validate(RefusjonskravDto::perioder).refujonsDagerIkkeOverstigerPeriodelengder()

            // kan ikke kreve refusjon for dager før 16. mars
            validate(RefusjonskravDto::perioder).refusjonsdagerInnenforGyldigPeriode(refusjonFraDato)

            // Summen av antallDagerMedRefusjon kan ikke overstige total periodelengde - 3 dager
            validate(RefusjonskravDto::perioder).arbeidsgiverBetalerForDager(arbeidsgiverBetalerForDager, refusjonFraDato)

            // opphold mellom periodene kan ikke overstige 16 dager
            validate(RefusjonskravDto::perioder).harMaksimaltOppholdMellomPerioder(maksOppholdMellomPerioder)

            // periodene kan ikke overlappe
            validate(RefusjonskravDto::perioder).harIngenOverlappendePerioder()

            // periodene til sammen kan ikke overstige 16
            validate(RefusjonskravDto::perioder).totalPeriodeLengdeErMaks(maksimalAGPLengde)

            // tom periode kan ikke ha refusjonsbeløp
            validate(RefusjonskravDto::perioder).tomPeriodeKanIkkeHaBeloepConstraint()
        }
    }
}
