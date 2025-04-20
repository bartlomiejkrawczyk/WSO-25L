package pl.edu.pw.ia.manager.domain.model

import pl.edu.pw.ia.heartbeat.domain.model.Address

sealed interface VirtualMachineConfig {
    val name: VirtualMachineName
    val address: Address
    val type: VirtualMachineType
}

data class Stateless(
    override val name: VirtualMachineName,
    override val address: Address,
) : VirtualMachineConfig {
    override val type: VirtualMachineType = VirtualMachineType.STATELESS
}

data class LoadBalancer(
    override val name: VirtualMachineName,
    override val address: Address,
    val workers: Collection<Address>,
) : VirtualMachineConfig {
    override val type: VirtualMachineType = VirtualMachineType.LOAD_BALANCER
}

enum class VirtualMachineType {
    STATELESS,
    LOAD_BALANCER,
}

@JvmInline
value class VirtualMachineName(val value: String)
