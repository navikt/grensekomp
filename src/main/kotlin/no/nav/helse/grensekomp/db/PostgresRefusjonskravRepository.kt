package no.nav.helse.grensekomp.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.domene.RefusjonskravStatus
import org.slf4j.LoggerFactory
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PostgresRefusjonskravRepository(val ds: DataSource, val mapper: ObjectMapper) : RefusjonskravRepository {
    private val logger = LoggerFactory.getLogger(PostgresRefusjonskravRepository::class.java)

    private val tableName = "refusjonskrav"
    private val getByVirksomhetsnummerStatement = """SELECT * FROM $tableName 
            WHERE data ->> 'virksomhetsnummer' = ?;"""

    private val getByStatuses = """SELECT * FROM $tableName 
            WHERE data ->> 'status' = ? LIMIT ?;"""

    private val getByNoKvitteringForVirksomhetStatement = """SELECT * FROM $tableName 
            WHERE (data ->> 'kvitteringId' IS NULL OR data ->> 'kvitteringId' = '')
            AND data ->> 'virksomhetsnummer' = ?;"""

    private val getOneVirksomhetWithoutKvitteringStatement = """SELECT data->>'virksomhetsnummer' FROM $tableName 
            WHERE data ->> 'kvitteringId' IS NULL OR data ->> 'kvitteringId' = ''
            LIMIT 1;"""

    private val getByIdStatement = """SELECT * FROM $tableName WHERE data ->> 'id' = ?"""

    private val saveStatement = "INSERT INTO $tableName (data) VALUES (?::json);"

    private val updateStatement = "UPDATE $tableName SET data = ?::json WHERE data ->> 'id' = ?;"

    private val getByIdentitetsnummerAndVirksomhetsnummerStatement = """SELECT * FROM $tableName 
         WHERE data ->> 'identitetsnummer' = ?
            AND data ->> 'virksomhetsnummer' = ?;"""

    private val deleteStatement = "DELETE FROM $tableName WHERE data ->> 'id' = ?"
    private val deleteAllStatement = "DELETE FROM $tableName"

    private val statsByWeekStatement = """
        SELECT
               extract('week' from to_date(data ->> 'opprettet', 'YYYY-MM-DD')) as uke,
               count(*) as antall,
               sum((data -> 'periode' ->> 'beregnetMånedsinntekt')::float)
        FROM refusjonskrav
        WHERE data ->> 'status' = 'SENDT_TIL_BEHANDLING'
        GROUP BY extract('week' from to_date(data ->> 'opprettet', 'YYYY-MM-DD'));
    """.trimIndent()

    private val getByIdentitetsnummerStatement = "SELECT * FROM $tableName WHERE data ->> 'identitetsnummer' = ?;"

    override fun statsByWeek(): Map<Int, Pair<Int, Float>> {
        ds.connection.use { con ->
            val resultMap = HashMap<Int, Pair<Int, Float>>()
            val res = con.prepareStatement(statsByWeekStatement).apply {
            }.executeQuery()

            while (res.next()) {
                resultMap[res.getInt(1)] = Pair(res.getInt(2), res.getFloat(3))
            }
            return resultMap
        }
    }

    override fun getAllForVirksomhet(virksomhetsnummer: String): List<Refusjonskrav> {
        ds.connection.use { con ->
            val resultList = ArrayList<Refusjonskrav>()
            val res = con.prepareStatement(getByVirksomhetsnummerStatement).apply {
                setString(1, virksomhetsnummer)
            }.executeQuery()

            while (res.next()) {
                resultList.add(extractRefusjonskrav(res))
            }
            return resultList
        }
    }

    override fun getAllForVirksomhetWithoutKvittering(virksomhetsnummer: String): List<Refusjonskrav> {
        ds.connection.use { con ->
            val resultList = ArrayList<Refusjonskrav>()
            val res = con.prepareStatement(getByNoKvitteringForVirksomhetStatement).apply {
                setString(1, virksomhetsnummer)
            }.executeQuery()

            while (res.next()) {
                resultList.add(extractRefusjonskrav(res))
            }
            return resultList
        }
    }


    override fun getRandomVirksomhetWithoutKvittering(): String? {
        ds.connection.use { con ->
            var result: String? = null
            val res = con.prepareStatement(getOneVirksomhetWithoutKvitteringStatement).executeQuery()
            while (res.next()) {
                result = res.getString(1)
            }
            return result
        }
    }

    override fun getByStatus(status: RefusjonskravStatus, limit: Int): List<Refusjonskrav> {
        ds.connection.use { con ->
            val resultList = ArrayList<Refusjonskrav>()
            val res = con.prepareStatement(getByStatuses).apply {
                setString(1, status.toString())
                setInt(2, limit)
            }.executeQuery()

            while (res.next()) {
                resultList.add(extractRefusjonskrav(res))
            }
            return resultList
        }
    }

    override fun bulkInsert(kravListe: List<Refusjonskrav>): List<Int> {
        logger.info("Starter serialisering av ${kravListe.size} krav")
        val jsonListe = kravListe.map { mapper.writeValueAsString(it) } // hold denne utenfor connection.use
        logger.info("Serialisering ferdig, starter en tilkobling og sender")
        ds.connection.use { con ->
            try {
                con.autoCommit = false

                val statement = con.prepareStatement(saveStatement, PreparedStatement.RETURN_GENERATED_KEYS)

                for (json in jsonListe) {
                    statement.setString(1, json)
                    statement.addBatch()
                }

                statement.executeBatch()
                con.commit()
                logger.info("Comittet")

                val referanseNummere = ArrayList<Int>(kravListe.size)
                while (statement.generatedKeys.next()) {
                    referanseNummere.add(statement.generatedKeys.getInt(2))
                }

                return@bulkInsert referanseNummere
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

    override fun bulkInsert(kravListe: List<Refusjonskrav>, connection: Connection): List<Int> {
        logger.info("Starter serialisering av ${kravListe.size} krav")
        val jsonListe = kravListe.map { mapper.writeValueAsString(it) } // hold denne utenfor connection.use
        logger.info("Serialisering ferdig, starter en tilkobling og sender")
        val statement = connection.prepareStatement(saveStatement, PreparedStatement.RETURN_GENERATED_KEYS)
        for (json in jsonListe) {
            statement.setString(1, json)
            statement.addBatch()
        }
        statement.executeBatch()
        val referanseNummere = ArrayList<Int>(kravListe.size)
        while (statement.generatedKeys.next()) {
            referanseNummere.add(statement.generatedKeys.getInt(2))
        }
        return@bulkInsert referanseNummere
    }

    override fun update(krav: Refusjonskrav) {
        val json = mapper.writeValueAsString(krav)
        ds.connection.use {
            it.prepareStatement(updateStatement).apply {
                setString(1, json)
                setString(2, krav.id.toString())
            }.executeUpdate()
        }
    }

    override fun update(krav: Refusjonskrav, connection: Connection) {
        val json = mapper.writeValueAsString(krav)

        connection.prepareStatement(updateStatement).apply {
            setString(1, json)
            setString(2, krav.id.toString())
        }.executeUpdate()
    }

    override fun getById(id: UUID): Refusjonskrav? {
        ds.connection.use {
            val existingYpList = ArrayList<Refusjonskrav>()
            val res = it.prepareStatement(getByIdStatement).apply {
                setString(1, id.toString())
            }.executeQuery()

            while (res.next()) {
                existingYpList.add(extractRefusjonskrav(res))
            }

            return existingYpList.firstOrNull()
        }
    }

    override fun getByIdentitetsnummer(identitetsnummer: String): List<Refusjonskrav> {
        ds.connection.use {
            val existingYpList = ArrayList<Refusjonskrav>()
            val res = it.prepareStatement(getByIdentitetsnummerStatement).apply {
                setString(1, identitetsnummer)
            }.executeQuery()

            while (res.next()) {
                existingYpList.add(extractRefusjonskrav(res))
            }

            return existingYpList
        }
    }

    private fun getKravById(id: UUID): ArrayList<Refusjonskrav> {
        ds.connection.use {
            val existingYpList = ArrayList<Refusjonskrav>()
            val res = it.prepareStatement(getByIdStatement).apply {
                setString(1, id.toString())
            }.executeQuery()

            while (res.next()) {
                existingYpList.add(extractRefusjonskrav(res))
            }

            return existingYpList
        }
    }

    override fun insert(refusjonskrav: Refusjonskrav): Refusjonskrav {
        val json = mapper.writeValueAsString(refusjonskrav)
        ds.connection.use {
            it.prepareStatement(saveStatement).apply {
                setString(1, json)
            }.executeUpdate()
        }
        return getById(refusjonskrav.id)
                ?: throw IOException("Unable to read receipt for refusjonskrav with id ${refusjonskrav.id}")
    }

    override fun insert(refusjonskrav: Refusjonskrav, connection: Connection): Refusjonskrav {
        val json = mapper.writeValueAsString(refusjonskrav)

        connection.prepareStatement(saveStatement).apply {
            setString(1, json)
        }.executeUpdate()

        return refusjonskrav
    }


    override fun getExistingRefusjonskrav(identitetsnummer: String, virksomhetsnummer: String): List<Refusjonskrav> {
        ds.connection.use {
            val existingYpList = ArrayList<Refusjonskrav>()
            val res = it.prepareStatement(getByIdentitetsnummerAndVirksomhetsnummerStatement).apply {
                setString(1, identitetsnummer)
                setString(2, virksomhetsnummer)
            }.executeQuery()

            while (res.next()) {
                existingYpList.add(extractRefusjonskrav(res))
            }

            return existingYpList
        }
    }

    override fun delete(id: UUID): Int {
        ds.connection.use {
            return it.prepareStatement(deleteStatement).apply {
                setString(1, id.toString())
            }.executeUpdate()
        }
    }

    fun deleteAll(): Int {
        ds.connection.use {
            return it.prepareStatement(deleteAllStatement).executeUpdate()
        }
    }

    private fun extractRefusjonskrav(res: ResultSet): Refusjonskrav {
        return mapper.readValue<Refusjonskrav>(res.getString("data"))
    }
}
