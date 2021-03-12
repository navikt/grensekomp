package no.nav.helse.grensekomp.prosessering.metrics

import no.nav.helse.grensekomp.domene.Refusjonskrav
import no.nav.helse.grensekomp.integration.sensu.SensuClient
import no.nav.helse.grensekomp.integration.sensu.SensuEvent
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.util.concurrent.TimeUnit

interface InfluxReporter {
    fun registerRefusjonskrav(krav: Refusjonskrav)
}

class InfluxReporterImpl(
        appName: String,
        cluster: String,
        namespace: String,
        private val sensuClient: SensuClient
) : InfluxReporter {

    val logger = LoggerFactory.getLogger(InfluxReporter::class.java)

    fun registerPoint(measurement: String, tags: Map<String, String>, fields: Map<String, Any>, nanos: Long) {
        val sensuEvent = createSensuEvent(Point.measurement(measurement)
                .time(nanos, TimeUnit.NANOSECONDS)
                .tag(tags)
                .tag(DEFAULT_TAGS)
                .fields(fields)
                .build().lineProtocol())
        sensuClient.write(sensuEvent)
        logger.info("sensuEvent: ${sensuEvent.json}")
    }

    private val DEFAULT_TAGS: Map<String, String> = java.util.Map.of(
            "application", appName,
            "cluster", cluster,
            "namespace", namespace
    )

    fun createSensuEvent(output: String): SensuEvent =
            SensuEvent("helsearbeidsgiver-grensekomp-events", output)

    override fun registerRefusjonskrav(krav: Refusjonskrav) {
        val instant = krav.opprettet.atZone(ZoneId.systemDefault()).toInstant()
        val nanos = TimeUnit.NANOSECONDS.convert(instant.epochSecond, TimeUnit.SECONDS)
        registerPoint(
                measurement = "grensekomp.refusjonskrav",
                tags = mapOf("kilde" to krav.kilde.substring(0,3)),
                fields = mapOf(
                        "antallPerioder" to krav.perioder.size,
                        "antallDagerRefusjon" to krav.perioder.sumBy { it.antallDagerMedRefusjon },
                        "totalBeloep" to krav.perioder.sumByDouble { it.beloep }),
                nanos = nanos + instant.nano)
        logger.info("registrerer info om krav ${krav.id} til influx")
    }
}
