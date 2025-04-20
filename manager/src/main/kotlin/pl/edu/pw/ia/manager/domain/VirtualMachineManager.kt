package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName

interface VirtualMachineManager {

    fun deleteAllVirtualMachines()

    fun listAvailableVirtualMachines(): Collection<VirtualMachineConfig>

    fun createVirtualMachine(config: VirtualMachineConfig)

    fun updateVirtualMachine(config: VirtualMachineConfig)

    fun deleteVirtualMachine(name: VirtualMachineName)

    fun findIp(name: VirtualMachineName): IpAddress?
}
