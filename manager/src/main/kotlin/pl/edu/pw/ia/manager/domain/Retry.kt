package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.infrastructure.logger
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Retry {

    private val logger = logger()

    suspend fun <T> retryUntilSuccess(
        interval: Duration = 10.seconds,
        timeout: Duration = 3.minutes,
        block: suspend () -> Result<T>,
    ): Result<T> {
        val start = System.currentTimeMillis()

        var attempts = 0

        while ((System.currentTimeMillis() - start).milliseconds < timeout) {
            attempts++

            val result = block()

            if (result.isSuccess) {
                return result
            }

            logger.info("Attempt $attempts failed. Retrying in $interval")
            kotlinx.coroutines.delay(interval)
        }

        return Result.failure(TimeoutException("Retry failed"))
    }
}
