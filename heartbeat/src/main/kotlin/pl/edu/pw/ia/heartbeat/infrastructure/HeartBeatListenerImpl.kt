package pl.edu.pw.ia.heartbeat.infrastructure

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import pl.edu.pw.ia.heartbeat.domain.HeartBeat
import pl.edu.pw.ia.heartbeat.domain.HeartBeatListener
import reactor.core.publisher.Flux
import kotlin.time.Duration
import kotlin.time.DurationUnit

class HeartBeatListenerImpl(
    private val webClient: WebClient,
) : HeartBeatListener {

    override fun listenForHeartBeat(delay: Duration): Flux<HeartBeat> =
        webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/heartbeat/forever")
                    .queryParam("delay", delay.toInt(DurationUnit.SECONDS))
                    .build()
            }
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(object : ParameterizedTypeReference<ServerSentEvent<HeartBeat>>() {})
            .mapNotNull { it.data() }
}
