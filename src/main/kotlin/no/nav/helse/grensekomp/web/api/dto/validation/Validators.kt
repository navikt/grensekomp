package no.nav.helse.grensekomp.web.dto.validation

import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator.Companion.tabeller.kontrollsiffer1
import no.nav.helse.grensekomp.web.dto.validation.FoedselsNrValidator.Companion.tabeller.kontrollsiffer2
import no.nav.helse.grensekomp.web.dto.validation.OrganisasjonsnummerValidator.Companion.tabeller.weights
import no.nav.helse.grensekomp.web.dto.validation.BostedlandValidator.Companion.tabeller.euLanderIsoKoder


class FoedselsNrValidator(input: String?) {
    val asString: String

    init {
        require(input != null)
        asString = input
        require("""\d{11}""".toRegex().matches(asString)) { "Ikke et gyldig fødselsnummer: $asString" }
        require(gyldigeKontrollsiffer) { "Kontrollsiffer må være gyldige" }
    }

    private val gyldigeKontrollsiffer: Boolean
        get() {
            val ks1 = asString[9].toString().toInt()
            val ks2 = asString[10].toString().toInt()

            val c1 = checksum(kontrollsiffer1, asString)
            if (c1 == 10 || c1 != ks1) {
                return false
            }

            val c2 = checksum(kontrollsiffer2, asString)
            if (c2 == 10 || c2 != ks2) {
                return false
            }
            return true
        }

    companion object {
        object tabeller {
            val kontrollsiffer1: List<Int> = listOf(3, 7, 6, 1, 8, 9, 4, 5, 2)
            val kontrollsiffer2: List<Int> = listOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
        }

        fun isValid(asString: String?): Boolean {
            return try {
                FoedselsNrValidator(asString)
                true
            } catch (t: Throwable) {
                false
            }
        }

        fun checksum(liste: List<Int>, str: String): Int {
            var sum = 0
            for ((i, m) in liste.withIndex()) {
                sum += m * str[i].toString().toInt()
            }

            val res = 11 - (sum % 11)
            return if (res == 11) 0 else res
        }
    }
}

/**
 * Sjekker at strengen er et gydlig org nummer ifølge:
 * https://www.brreg.no/om-oss/oppgavene-vare/alle-registrene-vare/om-enhetsregisteret/organisasjonsnummeret/
 */
class OrganisasjonsnummerValidator(input: String?) {
    val asString: String

    init {
        require(input != null)
        asString = input
        require("""\d{9}""".toRegex().matches(asString)) { "Ikke et gyldig organisasjonsnummer: $asString" }
        require(gyldigKontrollsiffer) { "Kontrollsiffer må være gyldige" }
    }

    private val gyldigKontrollsiffer: Boolean
        get() {
            val kontrollsiffer = asString[8].toString().toInt()
            val kalulertKontrollsiffer = checksum(weights, asString)
            if (kalulertKontrollsiffer == 10 || kalulertKontrollsiffer != kontrollsiffer) {
                return false
            }

            return true
        }

    companion object {
        object tabeller {
            val weights: List<Int> = listOf(3, 2, 7, 6, 5, 4, 3, 2)
        }

        fun isValid(asString: String?): Boolean {
            return try {
                OrganisasjonsnummerValidator(asString)
                true
            } catch (t: Throwable) {
                false
            }
        }

        fun checksum(kontrollsifferVekter: List<Int>, orgNr: String): Int {
            var produktsum = 0
            for ((i, m) in kontrollsifferVekter.withIndex()) {
                produktsum += m * orgNr[i].toString().toInt()
            }

            val res = 11 - (produktsum % 11)
            return if (res == 11) 0 else res
        }
    }
}

class BostedlandValidator(input: String?) {
    val asString: String

    init {
        require(input != null)
        asString = input
        require("""\d{3}""".toRegex().matches(asString)) { "Landskode må være 3 bokstave: $asString" }
        require(gyldigISoNavn) { "Ugyldig landskode" }
    }
    private val gyldigISoNavn: Boolean
    get() {
        return asString.toUpperCase() in euLanderIsoKoder
    }

    companion object {
        object tabeller {
            val euLanderIsoKoder = listOf(
                "BEL","BGR","DNK","EST","FIN","FRA","GRC","IRL","ISL","ITA","HRV","CYP",
                "LVA","LIE","LTU","LUX","MLT","NLD","POL","PRT","ROU","SVK","SVN","ESP",
                "GBR","CHE","SWE","CZE","DEU","HUN","AUT"
            )
            val euLander = listOf(
                "BELGIA","BULGARIA","DANMARK","ESTLAND","FINLAND","FRANKRIKE","HELLAS","IRLAND","ISLAND",
                "ITALIA","KROATIA","KYPROS","LATVIA","LIECHTENSTEIN","LITAUEN","LUXEMBOURG","MALTA",
                "NEDERLAND","POLEN","PORTUGAL","ROMANIA","SLOVAKIA","SLOVENIA","SPANIA","STORBRITANNIA OG NORD-IRLAND",
                "SVEITS","SVERIGE","TSJEKKIA","TYSKLAND","UNGARN","ØSTERRIKE"
            )
        }

        fun isValid(asString: String?): Boolean {
            return try {
                BostedlandValidator(asString)
                true
            } catch (t: Throwable) {
                false
            }
        }
    }
}
