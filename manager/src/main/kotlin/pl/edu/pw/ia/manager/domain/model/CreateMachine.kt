package pl.edu.pw.ia.manager.domain.model

data class CreateMachine(
    val name: VirtualMachineName,
    val ip: IpAddress,
)
