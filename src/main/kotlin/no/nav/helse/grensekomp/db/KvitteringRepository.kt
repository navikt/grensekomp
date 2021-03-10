package no.nav.helse.grensekomp.db

import no.nav.helse.grensekomp.kvittering.Kvittering
import no.nav.helse.grensekomp.kvittering.KvitteringStatus
import java.sql.Connection
import java.util.*

interface KvitteringRepository {

    fun insert(kvittering: Kvittering): Kvittering
    fun insert(kvittering: Kvittering, connection: Connection): Kvittering
    fun getByStatus(status: KvitteringStatus, limit: Int): List<Kvittering>
    fun getById(id: UUID): Kvittering?
    fun delete(id: UUID): Int
    fun update(kvittering: Kvittering)
    fun update(kvittering: Kvittering, connection: Connection)

}
