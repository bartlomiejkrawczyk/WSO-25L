package pl.edu.pw.ia.stateless.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.random.Random

@Configuration
class RandomConfiguration {

    @Bean
    fun random(): Random = Random(System.currentTimeMillis())
}
