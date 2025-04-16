package pl.edu.pw.ia.loadbalancer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class LoadBalancerApplication

fun main(args: Array<String>) {
    runApplication<LoadBalancerApplication>(*args)
}
