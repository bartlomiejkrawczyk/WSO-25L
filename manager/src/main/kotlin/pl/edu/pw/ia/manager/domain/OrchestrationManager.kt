package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.CreateMachine
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName

interface OrchestrationManager {

    fun listVirtualMachines(): Collection<VirtualMachineConfig>

    fun createVirtualMachine(request: CreateMachine)

    fun deleteVirtualMachine(name: VirtualMachineName)

    fun findIp(name: VirtualMachineName): IpAddress?

    fun registerConfigurationChanged(manager: Address, configs: Collection<VirtualMachineConfig>)

    fun becomeMaster()
}
