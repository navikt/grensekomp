package no.nav.helse.grensekomp

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbService
import no.nav.helse.arbeidsgiver.kubernetes.KubernetesProbeManager
import no.nav.helse.arbeidsgiver.kubernetes.LivenessComponent
import no.nav.helse.arbeidsgiver.kubernetes.ReadynessComponent
import no.nav.helse.arbeidsgiver.system.AppEnv
import no.nav.helse.arbeidsgiver.system.getEnvironment
import no.nav.helse.arbeidsgiver.system.getString
import no.nav.helse.grensekomp.koin.getAllOfType
import no.nav.helse.grensekomp.koin.selectModuleBasedOnProfile
import no.nav.helse.grensekomp.processing.brukernotifikasjon.BrukernotifikasjonProcessor
import no.nav.helse.grensekomp.processing.gravid.krav.GravidKravKafkaProcessor
import no.nav.helse.grensekomp.processing.gravid.krav.GravidKravKvitteringProcessor
import no.nav.helse.grensekomp.processing.gravid.krav.GravidKravProcessor
import no.nav.helse.grensekomp.processing.gravid.soeknad.GravidSoeknadKafkaProcessor
import no.nav.helse.grensekomp.processing.gravid.soeknad.GravidSoeknadKvitteringProcessor
import no.nav.helse.grensekomp.web.nais.nais
import no.nav.helse.grensekomp.processing.gravid.soeknad.GravidSoeknadProcessor
import no.nav.helse.grensekomp.processing.kronisk.krav.KroniskKravKafkaProcessor
import no.nav.helse.grensekomp.processing.kronisk.krav.KroniskKravKvitteringProcessor
import no.nav.helse.grensekomp.processing.kronisk.krav.KroniskKravProcessor
import no.nav.helse.grensekomp.processing.kronisk.soeknad.KroniskSoeknadKafkaProcessor
import no.nav.helse.grensekomp.processing.kronisk.soeknad.KroniskSoeknadProcessor
import no.nav.helse.grensekomp.processing.kronisk.soeknad.KroniskSoeknadKvitteringProcessor
import no.nav.helse.grensekomp.web.auth.localCookieDispenser
import no.nav.helse.grensekomp.web.grensekompModule
import org.flywaydb.core.Flyway
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.slf4j.LoggerFactory

class grensekompApplication(val port: Int = 8080) : KoinComponent {
    private val logger = LoggerFactory.getLogger(grensekompApplication::class.simpleName)
    private var webserver: NettyApplicationEngine? = null
    private var appConfig: HoconApplicationConfig = HoconApplicationConfig(ConfigFactory.load())
    private val runtimeEnvironment = appConfig.getEnvironment()

    @KtorExperimentalAPI
    fun start() {
        if (runtimeEnvironment == AppEnv.PREPROD || runtimeEnvironment == AppEnv.PROD) {
            logger.info("Sover i 30s i påvente av SQL proxy sidecar")
            Thread.sleep(30000)
        }

        startKoin { modules(selectModuleBasedOnProfile(appConfig)) }
        migrateDatabase()

        configAndStartBackgroundWorker()
        autoDetectProbeableComponents()
        configAndStartWebserver()
    }

    fun shutdown() {
        webserver?.stop(1000, 1000)
        get<BakgrunnsjobbService>().stop()
        stopKoin()
    }

    private fun configAndStartWebserver() {
        webserver = embeddedServer(Netty, applicationEngineEnvironment {
            config = appConfig
            connector {
                port = this@grensekompApplication.port
            }

            module {
                if (runtimeEnvironment != AppEnv.PROD) {
                    localCookieDispenser(config)
                }

                nais()
                grensekompModule(config)
            }
        })

        webserver!!.start(wait = false)
    }

    private fun configAndStartBackgroundWorker() {
        if (appConfig.getString("run_background_workers") == "true") {
            get<BakgrunnsjobbService>().apply {
                registrer(get<GravidSoeknadProcessor>())
                registrer(get<GravidSoeknadKafkaProcessor>())
                registrer(get<GravidSoeknadKvitteringProcessor>())

                registrer(get<GravidKravProcessor>())
                registrer(get<GravidKravKafkaProcessor>())
                registrer(get<GravidKravKvitteringProcessor>())

                registrer(get<KroniskSoeknadProcessor>())
                registrer(get<KroniskSoeknadKafkaProcessor>())
                registrer(get<KroniskSoeknadKvitteringProcessor>())

                registrer(get<KroniskKravProcessor>())
                registrer(get<KroniskKravKafkaProcessor>())
                registrer(get<KroniskKravKvitteringProcessor>())

                registrer(get<BrukernotifikasjonProcessor>())

                startAsync(true)
            }
        }
    }

    private fun migrateDatabase() {
        logger.info("Starter databasemigrering")

        Flyway.configure().baselineOnMigrate(true)
            .dataSource(GlobalContext.get().koin.get())
            .load()
            .migrate()

        logger.info("Databasemigrering slutt")
    }

    private fun autoDetectProbeableComponents() {
        val kubernetesProbeManager = get<KubernetesProbeManager>()

        getKoin().getAllOfType<LivenessComponent>()
            .forEach { kubernetesProbeManager.registerLivenessComponent(it) }

        getKoin().getAllOfType<ReadynessComponent>()
            .forEach { kubernetesProbeManager.registerReadynessComponent(it) }

        logger.debug("La til probeable komponenter")
    }
}


@KtorExperimentalAPI
fun main() {
    val logger = LoggerFactory.getLogger("main")

    Thread.currentThread().setUncaughtExceptionHandler { thread, err ->
        logger.error("uncaught exception in thread ${thread.name}: ${err.message}", err)
    }

    val application = grensekompApplication()
    application.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Fikk shutdown-signal, avslutter...")
        application.shutdown()
        logger.info("Avsluttet OK")
    })
}

