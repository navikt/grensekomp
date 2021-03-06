package no.nav.helse.grensekomp.integration.brreg

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

interface BrregClient {
    suspend fun getVirksomhetsNavn(orgnr: String): String
}

class MockBrregClient : BrregClient {
    override suspend fun getVirksomhetsNavn(orgnr: String): String {
        return "Stark Industries"
    }
}

class BrregClientImp(private val httpClient: HttpClient, private val om: ObjectMapper, private val berreUrl: String) :
    BrregClient {
    override suspend fun getVirksomhetsNavn(orgnr: String): String {
        return try {
            httpClient.get<UnderenheterNavnResponse>(
                url {
                    protocol = URLProtocol.HTTPS
                    host = berreUrl
                    path(orgnr)
                }).navn
        } catch (cause: ClientRequestException) {
            if (404 == cause.response.status.value) {
                "Ukjent arbeidsgiver"
            } else {
                throw cause
            }
        }
    }
}