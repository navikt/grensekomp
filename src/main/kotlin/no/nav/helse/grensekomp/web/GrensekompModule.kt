package no.nav.helse.grensekomp.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.config.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.util.*
import no.nav.helse.arbeidsgiver.system.AppEnv
import no.nav.helse.arbeidsgiver.system.getEnvironment
import no.nav.helse.arbeidsgiver.system.getString
import no.nav.helse.grensekomp.web.api.*
import no.nav.helse.grensekomp.web.api.grensekompRoutes
import no.nav.security.token.support.ktor.tokenValidationSupport
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.grensekompModule(config: ApplicationConfig = environment.config) {

    install(Authentication) {
        tokenValidationSupport(config = config)
    }

    configureCORSAccess(config)
    configureExceptionHandling()

    install(ContentNegotiation) {
        val commonObjectMapper = get<ObjectMapper>()
        register(ContentType.Application.Json, JacksonConverter(commonObjectMapper))
    }

    routing {
        val apiBasePath = config.getString("ktor.application.basepath")
        route("$apiBasePath/api/v1") {
            authenticate {
                systemRoutes()
                grensekompRoutes(get(), get(), get(),get())
                altinnRoutes(get())
            }
        }
    }
}

private fun Application.configureCORSAccess(config: ApplicationConfig) {
    install(CORS)
    {
        method(HttpMethod.Options)
        method(HttpMethod.Post)
        method(HttpMethod.Get)
        method(HttpMethod.Delete)

        when (config.getEnvironment()) {
            AppEnv.PROD -> host("arbeidsgiver.nav.no", schemes = listOf("https"))
            else -> anyHost()
        }

        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}
