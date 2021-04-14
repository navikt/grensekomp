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
                it.value.status = RefusjonskravStatus.MOTTATT
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
        ds.connection.use { con ->
            con.autoCommit = false
            val refusjonskrav = refusjonskravRepository.getById(id)
            if (refusjonskrav != null) {
                logger.debug("sletter krav ", refusjonskrav.id)
                refusjonskrav.status = RefusjonskravStatus.SLETTET
                refusjonskravRepository.update(refusjonskrav)
                lagreRefusjonskravJobb(refusjonskrav, con)
            }
            return refusjonskrav
        }
    }

    override fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav> {
        return refusjonskravRepository.getAllForVirksomhet(virksomhetsnummer)
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

    override fun getPersonKrav(identitetsnummer: String) : List<Refusjonskrav> {
        return refusjonskravRepository.getByIdentitetsnummer(identitetsnummer)
    }
}
