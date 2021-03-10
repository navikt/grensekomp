package no.nav.helse.grensekomp.processing.brukernotifikasjon

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.GravidTestData
import no.nav.helse.KroniskTestData
import no.nav.helse.grensekomp.db.GravidKravRepository
import no.nav.helse.grensekomp.db.GravidSoeknadRepository
import no.nav.helse.grensekomp.db.KroniskKravRepository
import no.nav.helse.grensekomp.db.KroniskSoeknadRepository
import no.nav.helse.grensekomp.integration.kafka.BrukernotifikasjonBeskjedSender
import no.nav.helse.grensekomp.processing.BakgrunnsJobbUtils
import no.nav.helse.grensekomp.processing.brukernotifikasjon.BrukernotifikasjonProcessor.Jobbdata.SkjemaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class BrukernotifikasjonProcessorTest {

    val ksRepo = mockk<KroniskSoeknadRepository>(relaxed = true)
    val kkRepo = mockk<KroniskKravRepository>(relaxed = true)
    val gkRepo = mockk<GravidKravRepository>(relaxed = true)
    val gsRepo = mockk<GravidSoeknadRepository>(relaxed = true)
    val kafkaSenderMock = mockk<BrukernotifikasjonBeskjedSender>(relaxed = true)

    val objectMapper = ObjectMapper().registerModules(KotlinModule(), JavaTimeModule())

    val prosessor = BrukernotifikasjonProcessor(gkRepo, gsRepo, kkRepo, ksRepo, objectMapper, kafkaSenderMock, "srvUsername")

    private val oppgaveId = 9999
    private val arkivReferanse = "12345"
    private var jobb = BakgrunnsJobbUtils.emptyJob()

    @BeforeEach
    fun setup() {
        every { ksRepo.getById(any()) } returns KroniskTestData.soeknadKronisk
        every { kkRepo.getById(any()) } returns KroniskTestData.kroniskKrav
        every { gkRepo.getById(any()) } returns GravidTestData.gravidKrav
        every { gsRepo.getById(any()) } returns GravidTestData.soeknadGravid

    }

    @Test
    fun `skal sende kafkamelding med brukernotifikasjon for Kronisk Krav`() {
        jobb = BakgrunnsJobbUtils.testJob(objectMapper.writeValueAsString(
            BrukernotifikasjonProcessor.Jobbdata(UUID.randomUUID(), SkjemaType.KroniskKrav))
        )
        prosessor.prosesser(jobb)

        verify(exactly = 1) { kkRepo.getById(any()) }
        verify(exactly = 1) { kafkaSenderMock.sendMessage(any(), any()) }
    }

    @Test
    fun `skal sende kafkamelding med brukernotifikasjon for Kronisk Søknad`() {
        jobb = BakgrunnsJobbUtils.testJob(objectMapper.writeValueAsString(
            BrukernotifikasjonProcessor.Jobbdata(UUID.randomUUID(), SkjemaType.KroniskSøknad))
        )
        prosessor.prosesser(jobb)

        verify(exactly = 1) { ksRepo.getById(any()) }
        verify(exactly = 1) { kafkaSenderMock.sendMessage(any(), any()) }
    }

    @Test
    fun `skal sende kafkamelding med brukernotifikasjon for Gravid Krav`() {
        jobb = BakgrunnsJobbUtils.testJob(objectMapper.writeValueAsString(
            BrukernotifikasjonProcessor.Jobbdata(UUID.randomUUID(), SkjemaType.GravidKrav))
        )
        prosessor.prosesser(jobb)

        verify(exactly = 1) { gkRepo.getById(any()) }
        verify(exactly = 1) { kafkaSenderMock.sendMessage(any(), any()) }
    }

    @Test
    fun `skal sende kafkamelding med brukernotifikasjon for Gravid Søknad`() {
        jobb = BakgrunnsJobbUtils.testJob(objectMapper.writeValueAsString(
            BrukernotifikasjonProcessor.Jobbdata(UUID.randomUUID(), SkjemaType.GravidSøknad))
        )
        prosessor.prosesser(jobb)

        verify(exactly = 1) { gsRepo.getById(any()) }
        verify(exactly = 1) { kafkaSenderMock.sendMessage(any(), any()) }
    }
}