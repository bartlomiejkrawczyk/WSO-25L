package pl.edu.pw.ia.manager.domain.model

sealed interface VirtualMachineConfig {
    val name: VirtualMachineName
    val address: Address
}

data class Stateless(
    override val name: VirtualMachineName,
    override val address: Address,
) : VirtualMachineConfig

class LoadBalancer(
    override val name: VirtualMachineName,
    override val address: Address,
    val workers: Collection<Address>,
) : VirtualMachineConfig

enum class VirtualMachineType {
    STATELESS,
    LOAD_BALANCER,
    MANAGER, // TODO: reconsider this one
}

@JvmInline
value class VirtualMachineName(val name: String)
