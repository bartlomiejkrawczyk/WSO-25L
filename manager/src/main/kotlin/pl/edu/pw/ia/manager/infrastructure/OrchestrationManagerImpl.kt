package pl.edu.pw.ia.manager.infrastructure

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.OrchestrationManager
import pl.edu.pw.ia.manager.domain.RemoteManagersView
import pl.edu.pw.ia.manager.domain.VirtualMachineManager
import pl.edu.pw.ia.manager.domain.VmLifecycleHandler
import pl.edu.pw.ia.manager.domain.model.CreateMachine
import pl.edu.pw.ia.manager.domain.model.LoadBalancer
import pl.edu.pw.ia.manager.domain.model.Stateless
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName

@Service
class OrchestrationManagerImpl(
    private val vmManager: VirtualMachineManager,
    private val remoteManagersView: RemoteManagersView,
    configuration: ApplicationConfiguration,
) : OrchestrationManager {

    private lateinit var loadBalancerHandler: VmLifecycleHandler
    private val lifecycleHandlers: MutableMap<VirtualMachineName, VmLifecycleHandler> = mutableMapOf()
    private val availableAddresses: MutableList<Address> = configuration.availableAddresses.toMutableList()

    @PostConstruct
    fun initialize() {
        vmManager.deleteAllVirtualMachines()
        // TODO: write vm config to a file and no startup reload from this config

        val tempAddress = getNewAddress()
        val config = LoadBalancer(
            name = VirtualMachineName(LOAD_BALANCER_NAME),
            address = tempAddress,
            workers = getWorkers(),
        )
        loadBalancerHandler = VmLifecycleHandlerImpl(initialConfig = config, manager = vmManager)
        loadBalancerHandler.createVirtualMachine()

        // TODO: on startup ask signal to other managers your workers
    }

    @PreDestroy
    fun destroy() {
        vmManager.deleteAllVirtualMachines()
    }

    override fun listVirtualMachines(): Collection<VirtualMachineConfig> {
        return lifecycleHandlers.values.map { it.config }
    }

    override fun createVirtualMachine(request: CreateMachine) {
        val name = request.name

        lifecycleHandlers[name]?.let { error("Duplicate VM name $name") }
        remoteManagersView.virtualMachineLocation(name)?.let { error("Duplicate VM name $name at location $it") }

        val handler = VmLifecycleHandlerImpl(
            initialConfig = Stateless(
                name = name,
                address = request.address,
            ),
            manager = vmManager,
        )

        handler.createVirtualMachine()

        lifecycleHandlers[request.name] = handler
        // TODO: callback to other managers
    }

    override fun deleteVirtualMachine(name: VirtualMachineName) {
        val handler = lifecycleHandlers[name]
        if (handler != null) {
            handler.deleteVirtualMachine()
            lifecycleHandlers.remove(name)
            // TODO: callback to other managers
            return
        }
        remoteManagersView.deleteVirtualMachineView(name)
    }

    override fun findIp(name: VirtualMachineName): IpAddress? {
        val localIp = lifecycleHandlers.values
            .map { it.config }
            .filter { it.name == name }
            .map { it.address.ip }
            .firstOrNull()

        if (localIp != null) {
            return localIp
        }

        return remoteManagersView.findConfiguration(name)?.address?.ip
    }

    override fun registerConfigurationChanged(
        manager: Address,
        configs: Collection<VirtualMachineConfig>
    ) {
        val config = loadBalancerHandler.config as LoadBalancer
        loadBalancerHandler.config = config.copy(
            workers = getWorkers(),
        )
    }

    private fun getWorkers(): Collection<Address> {
        val localWorkers = lifecycleHandlers.values.map { it.config.address }
        val remoteWorkers = remoteManagersView.listWorkers()
        return localWorkers + remoteWorkers
    }

    private fun getNewAddress(): Address {
        return availableAddresses.removeLast()
    }

    companion object {
        const val LOAD_BALANCER_NAME = "load-balancer"
    }
}
