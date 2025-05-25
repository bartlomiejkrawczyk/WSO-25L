package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig

interface VmLifecycleHandler {

    var config: VirtualMachineConfig

    fun updateConfigAndRecreate(newConfig: VirtualMachineConfig)

    fun createVirtualMachine()

    fun deleteVirtualMachine()
}
