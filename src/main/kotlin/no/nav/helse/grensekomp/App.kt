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
import no.nav.helse.grensekomp.datapakke.DatapakkePublisherJob
import no.nav.helse.grensekomp.koin.getAllOfType
import no.nav.helse.grensekomp.koin.selectModuleBasedOnProfile
import no.nav.helse.grensekomp.web.nais.nais
import no.nav.helse.grensekomp.web.auth.localCookieDispenser
import no.nav.helse.grensekomp.web.grensekompModule
import no.nav.helse.grensekomp.prosessering.kvittering.KvitteringProcessor
import no.nav.helse.grensekomp.prosessering.refusjonskrav.RefusjonskravProcessor
import no.nav.helse.grensekomp.prosessering.refusjonskrav.SletteRefusjonskravProcessor
import org.flywaydb.core.Flyway
import org.koin.core.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.slf4j.LoggerFactory

class GrensekompApplication(val port: Int = 8080) : KoinComponent {
    private val logger = LoggerFactory.getLogger(GrensekompApplication::class.simpleName)
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

        configAndStartBackgroundWorkers()
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
                port = this@GrensekompApplication.port
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

    private fun configAndStartBackgroundWorkers() {
        if (appConfig.getString("run_background_workers") == "true") {
            logger.info("Starter bakgrunnsjobber...")
            get<BakgrunnsjobbService>().apply {
                registrer(get<KvitteringProcessor>())
                registrer(get<RefusjonskravProcessor>())
                registrer(get<SletteRefusjonskravProcessor>())
                startAsync(true)
            }

            get<DatapakkePublisherJob>().startAsync(retryOnFail = true)
            logger.info("Startet!")
        }
    }

    private fun migrateDatabase() {
        logger.info("Starter databasemigrering")

        Flyway.configure().baselineOnMigrate(false)
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

    val application = GrensekompApplication()
    application.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Fikk shutdown-signal, avslutter...")
        application.shutdown()
        logger.info("Avsluttet OK")
    })
}

