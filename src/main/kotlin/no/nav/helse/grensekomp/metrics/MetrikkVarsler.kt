package no.nav.helse.grensekomp.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Summary
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsvarsler

const val METRICS_NS = "grensekomp"


class MetrikkVarsler() : Bakgrunnsvarsler {
    override fun rapporterPermanentFeiletJobb() {
        FEILET_JOBB_COUNTER.inc()
    }
}

val FEILET_JOBB_COUNTER = Counter.build()
        .name("grensekomp_feilet_jobb")
        .help("Countes the number of permanently failed jobs")
        .register()

val MANGLENDE_ARBEIDSFORHOLD = Counter.build()
        .name("grensekomp_mangler_arbeidsforhold")
        .labelNames("reason")
        .help("Antall ganger vi har fått bom i sjekken mot arbeidsforhold")
        .register()

val PDL_VALIDERINGER = Counter.build()
        .name("grensekomp_pdl_sjekker_feilet")
        .labelNames("reason")
        .help("Antall ganger vi har fått bom i sjekkene mot PDL")
        .register()


val INNKOMMENDE_REFUSJONSKRAV_COUNTER: Counter = Counter.build()
        .namespace(METRICS_NS)
        .name("inkommende_refusjonskrav")
        .help("Counts the number of incoming messages")
        .register()

val INNKOMMENDE_REFUSJONSKRAV_BELOEP_COUNTER: Counter = Counter.build()
        .namespace(METRICS_NS)
        .labelNames("bosted", "erEØSStatsborger" )
        .name("sum_refusjonskrav")
        .help("Counts the number of incoming messages")
        .register()

val JOURNALFOERING_COUNTER: Counter = Counter.build()
        .namespace(METRICS_NS)
        .name("journalfoering")
        .help("Counts number of created journalposts")
        .register()

val OPPGAVE_COUNTER: Counter = Counter.build()
        .namespace(METRICS_NS)
        .name("oppgave")
        .help("Counts number of created oppgaves")
        .register()

val KRAV_TIME: Summary = Summary.build()
        .namespace(METRICS_NS)
        .name("krav_time_ms")
        .help("Krav time in milliseconds.").register()



