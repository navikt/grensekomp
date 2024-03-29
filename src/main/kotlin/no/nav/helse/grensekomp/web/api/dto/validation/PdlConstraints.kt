package no.nav.helse.grensekomp.web.api.dto.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import io.ktor.util.*
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlHentFullPerson
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.metrics.PDL_VALIDERINGER
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation


class BosattINorgeConstraint : CustomConstraint
class NorskStatsborgerConstraint : CustomConstraint

@KtorExperimentalAPI
fun validerPdlBaserteRegler(personData: PdlHentFullPerson?, refusjonskrav: RefusjonskravDto) {

    fun isNullOrNullNode(node: JsonNode?) = node == null || node is NullNode

    val bosattINorge = personData?.hentPerson?.bostedsadresse?.any { adr ->
        val harKjentNorskAddress = !isNullOrNullNode(adr.matrikkeladresse) || !isNullOrNullNode(adr.vegadresse)
        val erUtvandretFraDenneAddressenFørCutoff =
            (adr.gyldigTilOgMed != null && adr.gyldigTilOgMed!!.toLocalDate().isBefore(Periode.refusjonFraDato))

        val erInnvandretTilDenneAddressenEtterPerioden =
            adr.gyldigFraOgMed != null && adr.gyldigFraOgMed!!.toLocalDate().isAfter(refusjonskrav.periode.tom)

        harKjentNorskAddress && !erUtvandretFraDenneAddressenFørCutoff && !erInnvandretTilDenneAddressenEtterPerioden
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
