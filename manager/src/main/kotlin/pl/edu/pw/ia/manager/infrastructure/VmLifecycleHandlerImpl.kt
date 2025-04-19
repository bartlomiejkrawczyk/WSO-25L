package pl.edu.pw.ia.manager.infrastructure

import pl.edu.pw.ia.heartbeat.domain.HeartBeatListener
import pl.edu.pw.ia.heartbeat.infrastructure.logger
import pl.edu.pw.ia.manager.domain.VirtualMachineManager
import pl.edu.pw.ia.manager.domain.VmLifecycleHandler
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import reactor.core.Disposable
import reactor.core.scheduler.Schedulers
import reactor.util.retry.Retry
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class VmLifecycleHandlerImpl(
    override val config: VirtualMachineConfig,
    private val manager: VirtualMachineManager,
    private val heartBeatListener: HeartBeatListener,
) : VmLifecycleHandler {

    private val logger = logger()
    private var disposable: Disposable? = null
//    private val mutex: Mutex = Mutex() // TODO: add mutex support to prevent inconsistent vm management

    override fun createVirtualMachine() {
        manager.createVirtualMachine(config)

        disposable = heartBeatListener.listenForHeartBeat(delay = 1.seconds)
            .timeout(2.seconds.toJavaDuration())
            .retryWhen(
                Retry.backoff(3, 100.milliseconds.toJavaDuration())
                    .jitter(0.2)
                    .doAfterRetry { retry ->
                        logger.info("Heart beat retry ${retry.totalRetriesInARow()} for $config")
                    }
            )
            .doOnError { throwable ->
                logger.error("Retries exhausted or other error occurred", throwable)
                recreateVirtualMachine()
            }
            .onErrorComplete()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    override fun deleteVirtualMachine() {
        disposable?.dispose()
        manager.deleteVirtualMachine(config.name)
    }

    private fun recreateVirtualMachine() {
        deleteVirtualMachine()
        createVirtualMachine()
    }
}
