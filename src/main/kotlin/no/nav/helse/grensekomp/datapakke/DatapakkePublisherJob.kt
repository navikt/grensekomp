package no.nav.helse.grensekomp.datapakke

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.utils.RecurringJob
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import no.nav.helse.grensekomp.db.RefusjonskravRepository
import org.slf4j.LoggerFactory
import java.time.Duration

class DatapakkePublisherJob(
    private val refusjonskravRepository: RefusjonskravRepository,
    private val httpClient: HttpClient,
    private val datapakkeApiUrl: String,
    private val datapakkeId: String,
) :
    RecurringJob(
        CoroutineScope(Dispatchers.IO),
        Duration.ofHours(4).toMillis()
    ) {

    override fun doJob() {
       val datapakkeTemplate = "datapakke/datapakke-grensekomp.json".loadFromResources()
        val stats = refusjonskravRepository.statsByWeek()

       val populatedDatapakke = datapakkeTemplate
           .replace("@ukeSerie", stats.keys.joinToString())
           .replace("@antallSerie", stats.values.map { it.first }.joinToString())
           .replace("@beløpSerie", stats.values.map { it.second }.joinToString())

        runBlocking {
            val response = httpClient.put<HttpResponse>("$datapakkeApiUrl/$datapakkeId") {
                body = populatedDatapakke
            }

            logger.info("Oppdaterte datapakke $datapakkeId med respons ${response.readText()}")
        }
    }
}