package no.nav.helse.grensekomp.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbRepository
import no.nav.helse.grensekomp.db.KvitteringRepository
import no.nav.helse.grensekomp.db.RefusjonskravRepository
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import no.nav.helse.grensekomp.kvittering.Kvittering
import no.nav.helse.grensekomp.kvittering.KvitteringStatus
import no.nav.helse.grensekomp.prosessering.kvittering.KvitteringJobData
import no.nav.helse.grensekomp.prosessering.kvittering.KvitteringProcessor
import no.nav.helse.grensekomp.prosessering.refusjonskrav.RefusjonskravJobData
import no.nav.helse.grensekomp.prosessering.refusjonskrav.RefusjonskravProcessor
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

class PostgresRefusjonskravService(
    val ds: DataSource,
    val refusjonskravRepository: RefusjonskravRepository,
    val kvitteringRepository: KvitteringRepository,
    val bakgrunnsjobbRepository: BakgrunnsjobbRepository,
    val mapper: ObjectMapper
) : RefusjonskravService {

    private val logger = LoggerFactory.getLogger(PostgresRefusjonskravService::class.java)

    override fun saveKravWithKvittering(krav: Refusjonskrav): Refusjonskrav {
        ds.connection.use {
            it.autoCommit = false

            val kvittering = Kvittering(
                    virksomhetsnummer = krav.virksomhetsnummer,
                    refusjonsListe = listOf(krav),
                    tidspunkt = LocalDateTime.now(),
                    status = KvitteringStatus.JOBB
            )
            val savedKvittering = kvitteringRepository.insert(kvittering, it)
            krav.kvitteringId = savedKvittering.id
            krav.status = RefusjonskravStatus.JOBB
            val savedKrav = refusjonskravRepository.insert(krav, it)
            lagreKvitteringJobb(savedKvittering, it)
            lagreRefusjonskravJobb(savedKrav, it)
            it.commit()
            return savedKrav
        }
    }


    override fun saveKravListWithKvittering(kravList: Map<Int, Refusjonskrav>): Map<Int, Refusjonskrav> {
        //Alle innsendingene må være på samme virksomhet
        ds.connection.use { con ->
            con.autoCommit = false
            val kvittering = Kvittering(
                    virksomhetsnummer = kravList.values.first().virksomhetsnummer,
                    refusjonsListe = kravList.values.toList(),
                    tidspunkt = LocalDateTime.now(),
                    status = KvitteringStatus.JOBB
            )
            val savedKvittering = kvitteringRepository.insert(kvittering, con)
            lagreKvitteringJobb(savedKvittering, con)
            val savedMap = mutableMapOf<Int, Refusjonskrav>()
            kravList.forEach {
                it.value.kvitteringId = savedKvittering.id
                it.value.status = RefusjonskravStatus.JOBB
                savedMap[it.key] = refusjonskravRepository.insert(it.value, con)
                lagreRefusjonskravJobb(it.value, con)
            }
            con.commit()
            return savedMap
        }

    }

    override fun getKrav(id: UUID): Refusjonskrav? {
        return refusjonskravRepository.getById(id)
    }

    override fun cancelKrav(id: UUID): Refusjonskrav? {
        val refusjonskrav = refusjonskravRepository.getById(id)
        if (refusjonskrav != null) {
            logger.debug("sletter krav ", refusjonskrav.id)
            refusjonskrav.status = RefusjonskravStatus.SLETTET
            refusjonskravRepository.update(refusjonskrav)
        }
        return refusjonskrav
    }

    override fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav> {
        return refusjonskravRepository.getAllForVirksomhet(virksomhetsnummer)
    }

    override fun bulkInsert(kravListe: List<Refusjonskrav>): List<Int> {
        ds.connection.use { con ->
            con.autoCommit = false
            try {
                val resultList = mutableListOf<Int>()
                kravListe.groupBy {
                    it.virksomhetsnummer
                }.forEach {
                    val savedKvittering = kvitteringRepository.insert(
                            Kvittering(virksomhetsnummer = it.key,
                                    refusjonsListe = it.value,
                                    status = KvitteringStatus.JOBB,
                                    tidspunkt = LocalDateTime.now()), con)
                    it.value.forEach { krav ->
                        krav.kvitteringId = savedKvittering.id
                        krav.status = RefusjonskravStatus.JOBB
                        lagreRefusjonskravJobb(krav, con)
                    }
                    lagreKvitteringJobb(savedKvittering, con)
                    resultList.addAll(refusjonskravRepository.bulkInsert(it.value, con))
                }
                con.commit()
                return resultList
            } catch (e: SQLException) {
                logger.error("Ruller tilbake bulkinnsetting")
                try {
                    con.rollback()
                } catch (ex: Exception) {
                    logger.error("Klarte ikke rulle tilbake bulkinnsettingen", ex)
                }
                throw e
            }

        }
    }

    fun lagreKvitteringJobb(kvittering: Kvittering, connection: Connection) {
        bakgrunnsjobbRepository.save(
                Bakgrunnsjobb(
                        type = KvitteringProcessor.JOBB_TYPE,
                        data = mapper.writeValueAsString(KvitteringJobData(kvittering.id)),
                        maksAntallForsoek = 14
                ), connection)
    }

    fun lagreRefusjonskravJobb(refusjonskrav: Refusjonskrav, connection: Connection) {
        bakgrunnsjobbRepository.save(
                Bakgrunnsjobb(
                        type = RefusjonskravProcessor.JOBB_TYPE,
                        data = mapper.writeValueAsString(
                            RefusjonskravJobData(
                                refusjonskrav.id
                        )
                        ),
                        maksAntallForsoek = 14
                ), connection
        )
    }
}
