package no.nav.helse.grensekomp.kvittering

interface KvitteringSender {
    fun send(kvittering: Kvittering)
}