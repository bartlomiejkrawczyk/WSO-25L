package pl.edu.pw.ia.manager.infrastructure

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.RemoteManagerClient
import pl.edu.pw.ia.manager.domain.RemoteManagersView
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import pl.edu.pw.ia.manager.domain.model.VirtualMachineType

@Service
class RemoteManagersViewImpl(
    val managerClient: RemoteManagerClient,
) : RemoteManagersView {

    private val configurations: MutableMap<Address, Collection<VirtualMachineConfig>> = mutableMapOf()

    @PostConstruct
    fun initialize() {
        val config = managerClient.requestConfiguration()
        configurations.putAll(config)
    }

    @PreDestroy
    fun destroy() {
        managerClient.signalConfigurationChange(emptyList())
    }

    override fun virtualMachineLocation(name: VirtualMachineName): Address? {
        return configurations.entries
            .flatMap { (manager, configs) -> configs.map { manager to it } }
            .find { it.second.name == name }
            ?.first
    }

    override fun listWorkers(): Collection<Address> {
        return configurations.values.flatten()
            .filter { it.type == VirtualMachineType.STATELESS }
            .map { it.address }
    }

    override fun findConfiguration(name: VirtualMachineName): VirtualMachineConfig? {
        return configurations.values.flatten().find { it.name == name }
    }

    override fun signalConfigurationChange(configs: Collection<VirtualMachineConfig>) {
        managerClient.signalConfigurationChange(configs)
    }

    override fun registerConfigurationChanged(manager: Address, configs: Collection<VirtualMachineConfig>) {
        val oldConfig = configurations[manager]
        if (oldConfig == configs) {
            return
        }
        configurations[manager] = configs
    }
}
