package pl.edu.pw.ia.stateless.application

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Random")
@ApiResponse(
    responseCode = "500",
    description = "Internal Server Error."
)
@ApiResponse(responseCode = "200", description = "OK.")
interface RandomValueController {

    @Operation(summary = "Return boolean with given probability")
    fun generateRandomBoolean(probability: Double = 0.5): Mono<ValueWrapper<Boolean>>

    @Operation(summary = "Return random number. Numbers are distributed uniformly")
    fun generateRandomNumber(from: Int = 0, to: Int = 100): Mono<ValueWrapper<Int>>

    @Operation(summary = "Return random floating point number. Numbers are distributed uniformly")
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
        @RequestParam(required = false, defaultValue = "0.5") probability: Double
    ): Mono<ValueWrapper<Boolean>> =
        Mono.just(random.nextDouble() < probability).map(::ValueWrapper)

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
