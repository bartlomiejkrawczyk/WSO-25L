package pl.edu.pw.ia.manager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
@EnableConfigurationProperties
class ManagerApplication

fun main(args: Array<String>) {
    runApplication<ManagerApplication>(*args)
}
