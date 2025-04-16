package pl.edu.pw.ia.stateless

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class StatelessApplication

fun main(args: Array<String>) {
    runApplication<StatelessApplication>(*args)
}
