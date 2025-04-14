package pl.edu.pw.ia.heartbeat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class HeartBeatApplication


fun main(args: Array<String>) {
    runApplication<HeartBeatApplication>(*args)
}
