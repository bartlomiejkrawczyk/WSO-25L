package pl.edu.pw.ia.heartbeat.domain

data class HeartBeat(val status: HeartBeatStatus)

enum class HeartBeatStatus {
    OK,
}
