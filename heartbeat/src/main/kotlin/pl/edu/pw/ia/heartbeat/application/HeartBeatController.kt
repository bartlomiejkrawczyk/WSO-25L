package pl.edu.pw.ia.heartbeat.application

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.ia.heartbeat.domain.HeartBeat
import pl.edu.pw.ia.heartbeat.domain.HeartBeatStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

interface HeartBeatController {

    fun beatOnce(): Mono<HeartBeat>

    fun beatForever(delay: Int): Flux<ServerSentEvent<HeartBeat>>
}


@RestController
@RequestMapping("/heartbeat")
class HeartBeatControllerImpl : HeartBeatController {

    @GetMapping("/once", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    override fun beatOnce(): Mono<HeartBeat> {
        return Mono.just(
            HeartBeat(
                status = HeartBeatStatus.OK,
            )
        )
    }

    @GetMapping("/forever", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseStatus(HttpStatus.OK)
    override fun beatForever(@RequestParam(required = false, defaultValue = "1") delay: Int): Flux<ServerSentEvent<HeartBeat>> {
        return Flux.interval(delay.seconds.toJavaDuration())
            .map {
                ServerSentEvent.builder<HeartBeat>()
                    .data(
                        HeartBeat(
                            status = HeartBeatStatus.OK,
                        )
                    )
                    .build()
            }
    }
}
