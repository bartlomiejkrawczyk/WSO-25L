package pl.edu.pw.ia.manager.domain.model

import pl.edu.pw.ia.heartbeat.domain.model.Address

data class CreateMachine(
    val name: VirtualMachineName,
    val address: Address,
)
