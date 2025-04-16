package pl.edu.pw.ia.stateless.application

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.ia.heartbeat.application.ValueWrapper
import reactor.core.publisher.Mono
import kotlin.random.Random

interface RandomValueController {

    fun generateRandomBoolean(trueProbability: Double = 0.5): Mono<ValueWrapper<Boolean>>

    fun generateRandomNumber(from: Int = 0, to: Int = 100): Mono<ValueWrapper<Int>>

    fun generateRandomFloatingPoint(from: Double = 0.0, to: Double = 1.0): Mono<ValueWrapper<Double>>
}

@RestController
@RequestMapping("/random", produces = [MediaType.APPLICATION_JSON_VALUE])
class RandomValueControllerImpl(
    val random: Random,
) : RandomValueController {

    @GetMapping("/boolean")
    @ResponseStatus(HttpStatus.OK)
    override fun generateRandomBoolean(
        @RequestParam(required = false, defaultValue = "0.5") trueProbability: Double
    ): Mono<ValueWrapper<Boolean>> =
        Mono.just(random.nextDouble() < trueProbability).map(::ValueWrapper)

    @GetMapping("/number")
    @ResponseStatus(HttpStatus.OK)
    override fun generateRandomNumber(
        @RequestParam(required = false, defaultValue = "0") from: Int,
        @RequestParam(required = false, defaultValue = "100") to: Int
    ): Mono<ValueWrapper<Int>> =
        Mono.just(random.nextInt(from, to)).map(::ValueWrapper)

    @GetMapping("/floating")
    @ResponseStatus(HttpStatus.OK)
    override fun generateRandomFloatingPoint(
        @RequestParam(required = false, defaultValue = "0.0") from: Double,
        @RequestParam(required = false, defaultValue = "1.0") to: Double
    ): Mono<ValueWrapper<Double>> =
        Mono.just(random.nextDouble(from, to)).map(::ValueWrapper)
}
