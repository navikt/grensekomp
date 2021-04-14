package no.nav.helse.grensekomp.web.auth

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import no.nav.helse.arbeidsgiver.web.auth.AltinnAuthorizer
import no.nav.helse.grensekomp.integration.altinn.ManglerAltinnRettigheterException
import no.nav.security.token.support.core.jwt.JwtToken
import java.time.Instant
import java.util.*

@KtorExperimentalAPI
fun PipelineContext<Unit, ApplicationCall>.authorize(authorizer: AltinnAuthorizer, arbeidsgiverId: String) {
    val identitetsnummer = hentIdentitetsnummerFraLoginToken(application.environment.config, call.request)
    if (!authorizer.hasAccess(identitetsnummer, arbeidsgiverId)) {
        throw ManglerAltinnRettigheterException()
    }
}

@KtorExperimentalAPI
fun hentIdentitetsnummerFraLoginToken(config: ApplicationConfig, request: ApplicationRequest): String {
    val tokenString = getTokenString(config, request)
    return JwtToken(tokenString).subject
}

@KtorExperimentalAPI
fun hentUtløpsdatoFraLoginToken(config: ApplicationConfig, request: ApplicationRequest): Date {
    val tokenString = getTokenString(config, request)
    return JwtToken(tokenString).jwtTokenClaims.expirationTime ?: Date.from(Instant.MIN)
}

private fun getTokenString(config: ApplicationConfig, request: ApplicationRequest): String {
    val cookieName = config.configList("no.nav.security.jwt.issuers")[0].property("cookie_name").getString()

    return request.cookies[cookieName]
        ?: request.headers["Authorization"]?.replaceFirst("Bearer ", "")
        ?: throw IllegalAccessException("Du må angi et identitetstoken som cookieen $cookieName eller i Authorization-headeren")
}
