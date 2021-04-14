package no.nav.helse.grensekomp.web.api.dto.validation

import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlHentFullPerson
import no.nav.helse.grensekomp.metrics.PDL_VALIDERINGER
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation


class BosattINorgeConstraint : CustomConstraint
class NorskStatsborgerConstraint : CustomConstraint

@KtorExperimentalAPI
fun validerPdlBaserteRegler(personData: PdlHentFullPerson?, refusjonskrav: RefusjonskravDto) {

    val bosattINorge = personData?.hentPerson?.bostedsadresse?.any { adr ->
        adr.matrikkeladresse != null || adr.ukjentBosted != null || adr.vegadresse != null
    } ?: false

    if (bosattINorge) {
        PDL_VALIDERINGER.labels("bosatt_i_norge").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "identitetsnummer",
                    constraint = BosattINorgeConstraint(),
                    value = refusjonskrav.identitetsnummer
                )
            )
        )
    }

    val norskStatsborger = personData?.hentPerson?.statsborgerskap?.any { s -> s.land == "NOR" } ?: false
    if (norskStatsborger) {
        PDL_VALIDERINGER.labels("norsk_statsborger").inc()
        throw ConstraintViolationException(
            setOf(
                DefaultConstraintViolation(
                    "identitetsnummer",
                    constraint = NorskStatsborgerConstraint(),
                    value = refusjonskrav.identitetsnummer
                )
            )
        )
    }
}
