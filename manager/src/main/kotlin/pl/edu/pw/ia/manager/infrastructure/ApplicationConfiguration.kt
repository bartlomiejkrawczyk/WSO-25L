package pl.edu.pw.ia.manager.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import pl.edu.pw.ia.heartbeat.domain.model.Address

@Configuration
@ConfigurationProperties(prefix = "application")
data class ApplicationConfiguration(
    val managerAddress: Address? = null,
    var managers: Collection<Address> = emptyList(),
    var publicAddress: Address? = null,
    var availableAddresses: Collection<Address> = emptyList(),
    var master: Boolean = false,
)
