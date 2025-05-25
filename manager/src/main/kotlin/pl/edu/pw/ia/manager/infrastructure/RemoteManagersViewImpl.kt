package pl.edu.pw.ia.manager.infrastructure

import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.RemoteManagersView
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import pl.edu.pw.ia.manager.domain.model.VirtualMachineType

@Service
class RemoteManagersViewImpl(
    val configuration: ApplicationConfiguration,
) : RemoteManagersView {

    private val configurations: MutableMap<Address, Collection<VirtualMachineConfig>> = mutableMapOf()

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

    override fun registerConfigurationChanged(manager: Address, configs: Collection<VirtualMachineConfig>) {
        val oldConfig = configurations[manager]
        if (oldConfig == configs) {
            return
        }
        configurations[manager] = configs
    }
}
