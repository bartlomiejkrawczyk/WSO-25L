package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig

interface RemoteManagerClient {

    fun requestConfiguration(): Map<Address, Collection<VirtualMachineConfig>>

    fun signalConfigurationChange(configs: Collection<VirtualMachineConfig>)

    fun requestNewMaster()
}
