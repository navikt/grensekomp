package no.nav.helse.grensekomp.web.api.dto.validation

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlHentFullPerson
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlIdent
import no.nav.helse.grensekomp.TestData
import no.nav.helse.grensekomp.domene.Periode
import org.junit.jupiter.api.Test
import java.time.LocalDate.of
import java.time.LocalDateTime

internal class PdlConstraintsKtTest {
    val om = ObjectMapper().registerModules(KotlinModule(), JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

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
    @Test
    fun `Skal tåle json noder som null objekter`() {
        val pdlInfo = om.readValue<PdlHentFullPerson>("""
            {
                "hentIdenter": null,
                "hentGeografiskTilknytning": null,
                "hentPerson": {
                  "navn": [
                    {
                      "fornavn": "ARNE",
                      "mellomnavn": null,
                      "etternavn": "FRU",
                      "metadata": {
                        "master": "Freg"
                      }
                    }
                  ],
                  "bostedsadresse": [
                    {
                      "gyldigFraOgMed": "2013-12-05T00:00",
                      "angittFlyttedato": "2013-12-05",
                      "gyldigTilOgMed": null,
                      "vegadresse": null,
                      "matrikkeladresse": null,
                      "ukjentBosted": {
                        "bostedskommune": "1108"
                      },
                      "utenlandskAdresse": null
                    }
                  ],
                  "statsborgerskap": [
                    {
                      "land": "SWE"
                    }
                  ],
                  "foedsel": [
                    {
                      "foedselsdato": "1944-01-01"
                    }
                  ],
                  "doedsfall": [],
                  "adressebeskyttelse": [],
                  "kjoenn": [
                    {
                      "kjoenn": "MANN"
                    }
                  ]
              }
            }
        """.trimIndent())

        val soeknad = TestData.gyldigSoeknad.copy(periode = Periode(of(2021, 2, 1), of(2021, 4, 30), 25000))

        validerPdlBaserteRegler(pdlInfo, soeknad)
    }
}