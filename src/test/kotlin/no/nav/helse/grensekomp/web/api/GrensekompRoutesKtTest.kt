package no.nav.helse.grensekomp.web.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.Arbeidsforhold
import no.nav.helse.grensekomp.domene.Periode
import no.nav.helse.grensekomp.web.api.dto.RefusjonskravDto
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ArbeidsforholdContraintTest {
    @Test
    internal fun test() {
        val krav = RefusjonskravDto(
            "10107400090",
            "810007842",
            Periode(
                LocalDate.of(2021, 3, 9),
                LocalDate.of(2021, 3, 15),
                5,
                100.0
            )
        )

        val aaregMock = object: AaregArbeidsforholdClient {
            override suspend fun hentArbeidsforhold(ident: String, callId: String): List<Arbeidsforhold> {
                return ObjectMapper().registerModules(KotlinModule(), JavaTimeModule()).readValue("""
                    [
                        {
                            "arbeidsgiver": {
                                "type": "Organisasjon",
                                "organisasjonsnummer": "810007842"
                            },
                            "opplysningspliktig": {
                                "type": "Organisasjon",
                                "organisasjonsnummer": "810007672"
                            },
                            "arbeidsavtaler": [
                                {
                                    "stillingsprosent": 100.0,
                                    "gyldighetsperiode": {
                                        "fom": "2001-03-01",
                                        "tom": null
                                    }
                                }
                            ],
                            "ansettelsesperiode": {
                                "periode": {
                                    "fom": "2001-03-08",
                                    "tom": null
                                }
                            }
                        }
                    ]
                """.trimIndent())
            }
        }


        runBlocking {
            validerArbeidsforhold(aaregMock, krav)
        }


    }
}