package pl.edu.pw.ia.manager.application.model

import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.model.Stateless
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import pl.edu.pw.ia.manager.domain.model.VirtualMachineType

data class VirtualMachineConfigDTO(
    val name: String,
    val address: Address,
    val type: VirtualMachineType,
) {
    fun toDomain(): VirtualMachineConfig = Stateless(
        name = VirtualMachineName(name),
        address = address
    )
}

fun VirtualMachineConfig.toDTO() = VirtualMachineConfigDTO(
    name = name.value,
    address = address,
    type = type,
)
