package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName

interface RemoteManagersView {

    fun virtualMachineLocation(name: VirtualMachineName): Address?

    fun listWorkers(): Collection<Address>

    fun registerConfigurationChanged(manager: Address, configs: Collection<VirtualMachineConfig>)
}
