package pl.edu.pw.ia.heartbeat.domain

import reactor.core.publisher.Flux
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface HeartBeatListener {

    fun listenForHeartBeat(delay: Duration = 1.seconds): Flux<HeartBeat>
}
