package pl.edu.pw.ia.heartbeat.domain.model

data class Address(
    val ip: IpAddress,
    val port: Port,
) {
    constructor(url: String) : this(
        ip = IpAddress(url.substringAfter("://").substringBefore(":")),
        port = Port(url.substringAfterLast(":").toInt()),
    )

    fun toUrl(): String = "http://$ip:$port"
}

@JvmInline
value class IpAddress(val ip: String) {
    override fun toString(): String {
        return ip
    }
}

@JvmInline
value class Port(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }
}
