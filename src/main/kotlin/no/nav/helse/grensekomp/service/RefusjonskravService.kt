package no.nav.helse.grensekomp.service

import no.nav.helse.grensekomp.domene.Refusjonskrav
import java.util.*

interface RefusjonskravService {

    fun saveKravWithKvittering(krav: Refusjonskrav): Refusjonskrav
    fun saveKravListWithKvittering(kravList: Map<Int, Refusjonskrav>): Map<Int, Refusjonskrav>
    fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav>
    fun bulkInsert(kravListe: List<Refusjonskrav>): List<Int>
    fun getKrav(id: UUID) : Refusjonskrav?
    fun cancelKrav(id: UUID) : Refusjonskrav?
}
