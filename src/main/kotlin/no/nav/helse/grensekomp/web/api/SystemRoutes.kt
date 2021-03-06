package no.nav.helse.grensekomp.web.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import no.nav.helse.grensekomp.web.auth.hentUtlĂ¸psdatoFraLoginToken

@KtorExperimentalAPI
fun Route.systemRoutes() {
    route("/login-expiry") {
        get {
            call.respond(HttpStatusCode.OK, hentUtlĂ¸psdatoFraLoginToken(application.environment.config, call.request))
        }
    }
}
