package no.nav.helse.grensekomp.service

import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.service.RefusjonskravService

class MockRefusjonskravService(val refusjonskravRepo: RefusjonskravRepository) : RefusjonskravService {

    override fun saveKravWithKvittering(krav: Refusjonskrav): Refusjonskrav {
        return refusjonskravRepo.insert(krav)
    }

    override fun saveKravListWithKvittering(kravList: Map<Int, Refusjonskrav>): Map<Int, Refusjonskrav> {
        var savedMap = mutableMapOf<Int, Refusjonskrav>()
        var i = 0
        kravList.forEach {
            savedMap[i++] = (refusjonskravRepo.insert(it.value))
        }
        return savedMap
    }

    override fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav> {
        return refusjonskravRepo.getAllForVirksomhet(virksomhetsnummer)
    }

    override fun bulkInsert(kravListe: List<Refusjonskrav>): List<Int> {
        return refusjonskravRepo.bulkInsert(kravListe)
    }

}
