package pl.edu.pw.ia.heartbeat.domain.model

data class Address(
    val ip: IpAddress,
    val port: Port,
) {
    fun toUrl(): String = "http://$ip:$port"
}

@JvmInline
value class IpAddress(val ip: String)

@JvmInline
value class Port(val value: Int)
