package no.nav.helse.grensekomp.kvittering

class DummyKvitteringSender: KvitteringSender{
    override fun send(kvittering: Kvittering) {
        println("Sender kvittering: ${kvittering.id}")
    }
}