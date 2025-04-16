package pl.edu.pw.ia.heartbeat.domain

import reactor.core.publisher.Flux

interface HeartBeatListener {

    fun listenForHeartBeat(): Flux<HeartBeat>
}
