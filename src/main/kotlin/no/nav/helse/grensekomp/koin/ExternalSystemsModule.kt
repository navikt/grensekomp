package no.nav.helse.grensekomp.koin

import io.ktor.config.*
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.OAuth2TokenProvider
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClient
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.AaregArbeidsforholdClientImpl
import no.nav.helse.arbeidsgiver.integrasjoner.altinn.AltinnRestClient
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.DokarkivKlient
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.DokarkivKlientImpl
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlient
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlientImpl
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClientImpl
import no.nav.helse.arbeidsgiver.system.getString
import no.nav.helse.arbeidsgiver.web.auth.AltinnOrganisationsRepository
import no.nav.helse.grensekomp.integration.GrunnbeløpClient
import no.nav.helse.grensekomp.integration.altinn.CachedAuthRepo
import no.nav.helse.grensekomp.integration.brreg.BrregClient
import no.nav.helse.grensekomp.integration.brreg.BrregClientImp
import no.nav.helse.grensekomp.integration.oauth2.DefaultOAuth2HttpClient
import no.nav.helse.grensekomp.integration.oauth2.OAuth2ClientPropertiesConfig
import no.nav.helse.grensekomp.integration.oauth2.TokenResolver
import no.nav.helse.grensekomp.integration.virusscan.ClamavVirusScannerImp
import no.nav.helse.grensekomp.integration.virusscan.VirusScanner
import no.nav.security.token.support.client.core.oauth2.ClientCredentialsTokenClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient
import no.nav.security.token.support.client.core.oauth2.TokenExchangeClient
import org.koin.core.module.Module
import org.koin.dsl.bind

fun Module.externalSystemClients(config: ApplicationConfig) {
    single {
        CachedAuthRepo(
            AltinnRestClient(
                config.getString("altinn.service_owner_api_url"),
                config.getString("altinn.gw_api_key"),
                config.getString("altinn.altinn_api_key"),
                config.getString("altinn.service_id"),
                get()
            )
        )
    } bind AltinnOrganisationsRepository::class

    single { GrunnbeløpClient(get()) }

    single {
        val clientConfig = OAuth2ClientPropertiesConfig(config)
        val tokenResolver = TokenResolver()
        val oauthHttpClient = DefaultOAuth2HttpClient(get())
        val accessTokenService = OAuth2AccessTokenService(
            tokenResolver,
            OnBehalfOfTokenClient(oauthHttpClient),
            ClientCredentialsTokenClient(oauthHttpClient),
            TokenExchangeClient(oauthHttpClient)
        )

        val azureAdConfig = clientConfig.clientConfig["azure_ad"] ?: error("Fant ikke config i application.conf")
        OAuth2TokenProvider(accessTokenService, azureAdConfig)
    } bind AccessTokenProvider::class

    single { PdlClientImpl(config.getString("pdl_url"), get(), get(), get()) } bind PdlClient::class
    single { AaregArbeidsforholdClientImpl(config.getString("aareg_url"), get(), get()) } bind AaregArbeidsforholdClient::class

    single { DokarkivKlientImpl(config.getString("dokarkiv.base_url"), get(), get()) } bind DokarkivKlient::class
    single { OppgaveKlientImpl(config.getString("oppgavebehandling.url"), get(), get()) } bind OppgaveKlient::class
    single {
        ClamavVirusScannerImp(
            get(),
            config.getString("clamav_url")
        )
    } bind VirusScanner::class
    single { BrregClientImp(get(), get(), config.getString("berreg_enhet_url")) } bind BrregClient::class
}
