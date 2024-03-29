package no.nav.helse.grensekomp.web.api.dto

import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.domene.Periode.Companion.refusjonFraDato
import no.nav.helse.grensekomp.domene.Periode.Companion.refusjonTilDato
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
    fun validate(fristMnd: Long) {
        validate(this) {
            validate(RefusjonskravDto::identitetsnummer).isValidIdentitetsnummer()
            validate(RefusjonskravDto::virksomhetsnummer).isValidOrganisasjonsnummer()
            validate(RefusjonskravDto::bekreftet).isTrue()

            validate(RefusjonskravDto::periode).validate {
                validate(Periode::beregnetMånedsinntekt).isPositive()

                validate(Periode::tom).isGreaterThanOrEqualTo(it.fom)
                validate(Periode::tom).isLessThanOrEqualTo(LocalDate.now())

                // kan ikke kreve refusjon for dager før første refusjonsdato eller etter siste refusjonsdato
                validate(Periode::fom).isGreaterThanOrEqualTo(refusjonFraDato)
                validate(Periode::tom).isLessThanOrEqualTo(refusjonTilDato)
                val minFraDatoNy = LocalDate.now().minusMonths(fristMnd).withDayOfMonth(1)
                validate(Periode::fom).validerInnenforFristen(minFraDatoNy)
            }
            validate(RefusjonskravDto::bostedsland).isValidBostedsland()
        }
    }
}
