package no.nav.helse.grensekomp.web.api.dto.validation

import com.fasterxml.jackson.databind.node.NullNode
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlHentFullPerson
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlIdent
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.domene.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate.of
import java.time.LocalDateTime

internal class PdlConstraintsKtTest {

    @Test
    fun `Skal godkjenne innvandring etter perioden`() {
        val pdlInfo = PdlHentFullPerson(
            PdlHentFullPerson.PdlFullPersonliste(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                listOf(
                    PdlHentFullPerson.PdlFullPersonliste.PdlBostedsadresse(
                    LocalDateTime.of(2021, 5,1, 0, 0),
                        null,
                         NullNode.getInstance(),
                        null,
                        null
                )),
                emptyList()
            ),

            PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent("aktør-id", PdlIdent.PdlIdentGruppe.AKTORID))),

            PdlHentFullPerson.PdlGeografiskTilknytning(
                PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.UTLAND,
                null,
                null,
                "SWE"
            )
        )

        val soeknad = TestData.gyldigSoeknad.copy(periode = Periode(of(2021, 2, 1), of(2021, 4, 30), 25000))

        validerPdlBaserteRegler(pdlInfo, soeknad)
    }


    @Test
    fun `Skal godkjenne med tom liste i boaddresse`() {
        val pdlInfo = PdlHentFullPerson(
            PdlHentFullPerson.PdlFullPersonliste(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList()
            ),

            PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent("aktør-id", PdlIdent.PdlIdentGruppe.AKTORID))),

            PdlHentFullPerson.PdlGeografiskTilknytning(
                PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.UTLAND,
                null,
                null,
                "SWE"
            )
        )

        val soeknad = TestData.gyldigSoeknad.copy(periode = Periode(of(2021, 2, 1), of(2021, 4, 30), 25000))

        validerPdlBaserteRegler(pdlInfo, soeknad)
    }

    @Test
    fun `Skal godkjenner utkjent bostedsadresse`() {
        val pdlInfo = PdlHentFullPerson(
            PdlHentFullPerson.PdlFullPersonliste(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                listOf(
                    PdlHentFullPerson.PdlFullPersonliste.PdlBostedsadresse(
                        LocalDateTime.of(2021, 5,1, 0, 0),
                        null,
                        null,
                        null,
                        NullNode.getInstance(),
                    )),
                emptyList()
            ),

            PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent("aktør-id", PdlIdent.PdlIdentGruppe.AKTORID))),

            PdlHentFullPerson.PdlGeografiskTilknytning(
                PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.UTLAND,
                null,
                null,
                "SWE"
            )
        )

        val soeknad = TestData.gyldigSoeknad.copy(periode = Periode(of(2021, 2, 1), of(2021, 4, 30), 25000))

        validerPdlBaserteRegler(pdlInfo, soeknad)
    }
}