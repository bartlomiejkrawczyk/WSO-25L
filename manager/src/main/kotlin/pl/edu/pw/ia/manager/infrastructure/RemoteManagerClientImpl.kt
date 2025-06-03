package pl.edu.pw.ia.manager.infrastructure

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.application.model.VirtualMachineConfigDTO
import pl.edu.pw.ia.manager.application.model.toDTO
import pl.edu.pw.ia.manager.domain.RemoteManagerClient
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig

@Service
class RemoteManagerClientImpl(
    val configuration: ApplicationConfiguration,
) : RemoteManagerClient {

    private val webClients: Map<Address, WebClient> = configuration.managers
        .associateWith { address ->
            WebClient.builder()
                .baseUrl(address.toUrl())
                .build()
        }

    override fun requestConfiguration(): Map<Address, Collection<VirtualMachineConfig>> {
        return webClients.entries.mapNotNull { (address, client) ->
            client.get()
                .uri("/callback")
                .retrieve()
                .bodyToMono<Collection<VirtualMachineConfigDTO>>()
                .onErrorComplete()
                .block()
                ?.map { it.toDomain() }
                ?.let { address to it }
        }
            .associate { it }
    }

    override fun signalConfigurationChange(configs: Collection<VirtualMachineConfig>) {
        webClients.values.forEach { client ->
            client.post()
                .uri { builder ->
                    builder
                        .path("/callback")
                        .queryParam("managerUrl", configuration.managerAddress?.toUrl())
                        .build()
                }
                .bodyValue(configs.map { it.toDTO() })
                .retrieve()
                .bodyToMono<Void>()
                .onErrorComplete()
                .block()
        }
    }

    override fun requestNewMaster() {
        webClients.values.asSequence()
            .filter { client ->
                client.post()
                    .uri("/callback/master")
                    .retrieve()
                    .bodyToMono<Boolean>()
                    .onErrorComplete()
                    .block()
                    ?: false
            }
            .first()
    }
}
