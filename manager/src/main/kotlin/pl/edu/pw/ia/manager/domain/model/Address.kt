package pl.edu.pw.ia.manager.domain.model

data class Address(
    val ip: IpAddress,
    val port: Port,
)

@JvmInline
value class IpAddress(val ip: String)

@JvmInline
value class Port(val value: Int)
