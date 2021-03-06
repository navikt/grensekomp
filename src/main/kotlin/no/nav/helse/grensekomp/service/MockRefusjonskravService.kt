package no.nav.helse.grensekomp.service

import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.service.RefusjonskravService
import java.util.*

class MockRefusjonskravService(val refusjonskravRepo: RefusjonskravRepository) : RefusjonskravService {
    override fun getKrav(id: UUID): Refusjonskrav? {
        TODO("Not yet implemented")
    }

    override fun cancelKrav(id: UUID): Refusjonskrav? {
        TODO("Not yet implemented")
    }

    override fun getPersonKrav(identitetsnummer: String): List<Refusjonskrav> {
        return refusjonskravRepo.getByIdentitetsnummer(identitetsnummer)
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
}
