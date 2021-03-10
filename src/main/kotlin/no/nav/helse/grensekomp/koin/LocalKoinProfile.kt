package no.nav.helse.grensekomp.koin

import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import io.ktor.util.*
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbRepository
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbService
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.PostgresBakgrunnsjobbRepository
import no.nav.helse.arbeidsgiver.system.getString
import no.nav.helse.arbeidsgiver.web.auth.AltinnAuthorizer
import no.nav.helse.arbeidsgiver.web.auth.DefaultAltinnAuthorizer
import no.nav.helse.grensekomp.db.*
import no.nav.helse.grensekomp.service.JoarkService
import no.nav.helse.grensekomp.service.OppgaveService
import no.nav.helse.grensekomp.service.PostgresRefusjonskravService
import no.nav.helse.grensekomp.kvittering.DummyKvitteringSender
import no.nav.helse.grensekomp.kvittering.KvitteringSender
import no.nav.helse.grensekomp.service.RefusjonskravService
import org.koin.dsl.bind
import org.koin.dsl.module
import javax.sql.DataSource


@KtorExperimentalAPI
fun localDevConfig(config: ApplicationConfig) = module {
    mockExternalDependecies()

    single { HikariDataSource(createHikariConfig(config.getjdbcUrlFromProperties(), config.getString("database.username"), config.getString("database.password"))) } bind DataSource::class

    single { PostgresBakgrunnsjobbRepository(get()) } bind BakgrunnsjobbRepository::class
    single { BakgrunnsjobbService(get()) }

    single { DefaultAltinnAuthorizer(get()) } bind AltinnAuthorizer::class

    single { PostgresRefusjonskravRepository(get(), get()) } bind RefusjonskravRepository::class
    single { PostgresKvitteringRepository(get(), get()) } bind KvitteringRepository::class
    single { PostgresRefusjonskravService(get(), get(), get(), get(), get()) } bind RefusjonskravService::class

    single { DefaultAltinnAuthorizer(get()) } bind AltinnAuthorizer::class
    single { JoarkService(get()) } bind JoarkService::class

    single { OppgaveService(get()) } bind OppgaveService::class
    single { DummyKvitteringSender() } bind KvitteringSender::class
}
