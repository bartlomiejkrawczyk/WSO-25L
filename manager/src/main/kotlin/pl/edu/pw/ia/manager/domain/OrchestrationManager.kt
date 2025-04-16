package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.manager.domain.model.CreateMachine
import pl.edu.pw.ia.manager.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName

interface OrchestrationManager {

    fun createVirtualMachine(request: CreateMachine)

    fun deleteVirtualMachine(name: VirtualMachineName)

    fun findIp(name: VirtualMachineName): IpAddress?
}
