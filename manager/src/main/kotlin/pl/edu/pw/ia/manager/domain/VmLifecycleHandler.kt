package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig

interface VmLifecycleHandler {

    val config: VirtualMachineConfig

    fun createVirtualMachine()

    fun deleteVirtualMachine()
}
