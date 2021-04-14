package no.nav.helse.grensekomp.koin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.arbeidsgiver.integrasjoner.aareg.*
import no.nav.helse.arbeidsgiver.integrasjoner.altinn.AltinnOrganisasjon
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.DokarkivKlient
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.JournalpostRequest
import no.nav.helse.arbeidsgiver.integrasjoner.dokarkiv.JournalpostResponse
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OppgaveKlient
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveRequest
import no.nav.helse.arbeidsgiver.integrasjoner.oppgave.OpprettOppgaveResponse
import no.nav.helse.arbeidsgiver.integrasjoner.pdl.*
import no.nav.helse.arbeidsgiver.utils.loadFromResources
import no.nav.helse.arbeidsgiver.web.auth.AltinnOrganisationsRepository
import no.nav.helse.grensekomp.integration.brreg.BrregClient
import no.nav.helse.grensekomp.integration.brreg.MockBrregClient
import no.nav.helse.grensekomp.integration.virusscan.MockVirusScanner
import no.nav.helse.grensekomp.integration.virusscan.VirusScanner
import org.koin.core.module.Module
import org.koin.dsl.bind

fun Module.mockExternalDependecies() {
    single { MockAltinnRepo(get()) } bind AltinnOrganisationsRepository::class

    single {
        object : DokarkivKlient {
            override fun journalførDokument(
                journalpost: JournalpostRequest,
                forsoekFerdigstill: Boolean,
                callId: String
            ): JournalpostResponse {
                return JournalpostResponse("arkiv-ref", true, "J", null, emptyList())
            }
        }
    } bind DokarkivKlient::class

    single {
        object : AaregArbeidsforholdClient {
            override suspend fun hentArbeidsforhold(ident: String, callId: String): List<Arbeidsforhold> =
                listOf<Arbeidsforhold>(Arbeidsforhold(Arbeidsgiver("test", "810007842"), Opplysningspliktig("Juice", "810007702"), emptyList(), Ansettelsesperiode(Periode(
                    no.nav.helse.grensekomp.domene.Periode.refusjonFraDato, null)), no.nav.helse.grensekomp.domene.Periode.refusjonFraDato.atStartOfDay()))
        }
    } bind AaregArbeidsforholdClient::class

    single {
        object : PdlClient {
            override fun fullPerson(ident: String) =
                PdlHentFullPerson(
                    PdlHentFullPerson.PdlFullPersonliste(
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList()
                    ),

                    PdlHentFullPerson.PdlIdentResponse(listOf(PdlIdent("aktør-id", PdlIdent.PdlIdentGruppe.AKTORID))),

                    PdlHentFullPerson.PdlGeografiskTilknytning(
                        PdlHentFullPerson.PdlGeografiskTilknytning.PdlGtType.UTLAND,
                        null,
                        null,
                        "SWE"
                    )
                )

            override fun personNavn(ident: String) =
                PdlHentPersonNavn.PdlPersonNavneliste(
                    listOf(
                        PdlHentPersonNavn.PdlPersonNavneliste.PdlPersonNavn(
                            "Ola",
                            "M",
                            "Avsender",
                            PdlPersonNavnMetadata("freg")
                        )
                    )
                )
        }

    } bind PdlClient::class

    single {
        object : OppgaveKlient {
            override suspend fun opprettOppgave(
                opprettOppgaveRequest: OpprettOppgaveRequest,
                callId: String
            ): OpprettOppgaveResponse = OpprettOppgaveResponse(1234)
        }
    } bind OppgaveKlient::class

    single { MockVirusScanner() } bind VirusScanner::class
    single { MockBrregClient() } bind BrregClient::class
}

class MockAltinnRepo(om: ObjectMapper) : AltinnOrganisationsRepository {
    private val mockList = "altinn-mock/organisasjoner-med-rettighet.json".loadFromResources()
    private val mockAcl = om.readValue<Set<AltinnOrganisasjon>>(mockList)
    override fun hentOrgMedRettigheterForPerson(identitetsnummer: String): Set<AltinnOrganisasjon> = mockAcl
}