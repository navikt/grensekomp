package no.nav.helse.grensekomp.db

import no.nav.helse.grensekomp.domain.GravidKrav
import no.nav.helse.grensekomp.domain.GravidSoeknad
import no.nav.helse.grensekomp.domain.KroniskKrav
import no.nav.helse.grensekomp.domain.KroniskSoeknad

interface GravidSoeknadRepository: SimpleJsonbRepository<GravidSoeknad>
interface GravidKravRepository: SimpleJsonbRepository<GravidKrav>
interface KroniskSoeknadRepository: SimpleJsonbRepository<KroniskSoeknad>
interface KroniskKravRepository: SimpleJsonbRepository<KroniskKrav>
