package pl.edu.pw.ia.manager.infrastructure

import pl.edu.pw.ia.heartbeat.domain.HeartBeatListener
import pl.edu.pw.ia.heartbeat.infrastructure.HeartBeatListenerImpl
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
    initialConfig: VirtualMachineConfig,
    private val manager: VirtualMachineManager,
    private var heartBeatListener: HeartBeatListener = HeartBeatListenerImpl(initialConfig.address),
    private val recreationCallback: (() -> Unit)? = null,
) : VmLifecycleHandler {

    private val logger = logger()
    private var disposable: Disposable? = null

    override var config: VirtualMachineConfig = initialConfig

    override fun updateConfigAndRecreate(newConfig: VirtualMachineConfig) {
        val previousAddress = config.address
        config = newConfig
        if (config.address != previousAddress) {
            disposable?.dispose()
            manager.updateVirtualMachine(config)
            heartBeatListener = HeartBeatListenerImpl(config.address)
            disposable = startListener()
        } else {
            manager.updateVirtualMachine(config)
        }
    }

    override fun createVirtualMachine() {
        manager.createVirtualMachine(config)
        disposable = startListener()
    }

    private fun startListener(): Disposable {
        return heartBeatListener.listenForHeartBeat(delay = 1.seconds)
            .doOnNext { heartBeat ->
                logger.info("Received heart beat from $config: $heartBeat")
            }
            .timeout(2.seconds.toJavaDuration())
            .retryWhen(
                Retry.backoff(4, 500.milliseconds.toJavaDuration())
                    .jitter(0.2)
                    .doAfterRetry { retry ->
                        logger.info("Heart beat retry ${retry.totalRetriesInARow()} for $config")
                    }
            )
            .onErrorComplete { throwable ->
                logger.error("Retries exhausted or other error occurred", throwable)
                recreateVirtualMachine()
                return@onErrorComplete true
            }
            .subscribeOn(Schedulers.boundedElastic())
            .delaySubscription(10.seconds.toJavaDuration())
            .subscribe()
    }

    override fun deleteVirtualMachine(hard: Boolean) {
        if (hard) {
            disposable?.dispose()
        }
        runCatching {
            manager.deleteVirtualMachine(config.name)
        }.onFailure {
            logger.error("Failed to delete VM ${config.name}", it)
        }
    }

    private fun recreateVirtualMachine() {
        deleteVirtualMachine(hard = false)
        logger.info("Deleted VM ${config.name}")
        recreationCallback?.invoke()
        logger.info("Recreating VM ${config.name}")
        createVirtualMachine()
        logger.info("Recreated VM ${config.name}")
    }
}
