package pl.edu.pw.ia.manager.domain

import pl.edu.pw.ia.heartbeat.infrastructure.logger
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object Retry {

    private val logger = logger()

    fun <T> retryUntilSuccess(
        interval: Duration = 10.seconds,
        timeout: Duration = 3.minutes,
        block: () -> Result<T>,
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
            Thread.sleep(interval.toJavaDuration())
//            kotlinx.coroutines.delay(interval)
        }

        return Result.failure(TimeoutException("Retry failed"))
    }

    fun retryUntilTrue(
        interval: Duration = 10.seconds,
        timeout: Duration = 3.minutes,
        block: () -> Boolean,
    ) {
        val start = System.currentTimeMillis()

        var attempts = 0

        while ((System.currentTimeMillis() - start).milliseconds < timeout) {
            attempts++

            val result = block()

            if (result) {
                return
            }
            logger.info("Attempt $attempts failed. Retrying in $interval")
            Thread.sleep(interval.toJavaDuration())
//            kotlinx.coroutines.delay(interval)
        }
        error("Retry failed")
    }

    fun <T> retryUntilNotNull(
        interval: Duration = 10.seconds,
        timeout: Duration = 3.minutes,
        block: () -> T?,
    ): T {
        val start = System.currentTimeMillis()

        var attempts = 0

        while ((System.currentTimeMillis() - start).milliseconds < timeout) {
            attempts++

            val result = block()

            if (result != null) {
                return result
            }
            logger.info("Attempt $attempts failed. Retrying in $interval")
            Thread.sleep(interval.toJavaDuration())
//            kotlinx.coroutines.delay(interval)
        }
        error("Retry failed")
    }
}
