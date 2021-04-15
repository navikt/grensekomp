package no.nav.helse.grensekomp.service

import no.nav.helse.grensekomp.domene.Refusjonskrav
import java.util.*

interface RefusjonskravService {
    fun saveKravListWithKvittering(kravList: Map<Int, Refusjonskrav>): Map<Int, Refusjonskrav>
    fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav>
    fun getKrav(id: UUID) : Refusjonskrav?
    fun cancelKrav(id: UUID) : Refusjonskrav?
    fun getPersonKrav(identitetsnummer: String) : List<Refusjonskrav>
}
