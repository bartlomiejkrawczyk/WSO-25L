package pl.edu.pw.ia.manager.infrastructure

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.OrchestrationManager
import pl.edu.pw.ia.manager.domain.RemoteManagerClient
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
    private val configuration: ApplicationConfiguration,
    private val managerClient: RemoteManagerClient,
) : OrchestrationManager {

    private lateinit var loadBalancerHandler: VmLifecycleHandler
    private val lifecycleHandlers: MutableMap<VirtualMachineName, VmLifecycleHandler> = mutableMapOf()
    private val availableAddresses: MutableList<Address> = configuration.availableAddresses.toMutableList()

    @PostConstruct
    fun initialize() {
        vmManager.deleteAllVirtualMachines()

        createVirtualMachine(request = CreateMachine(name = VirtualMachineName(DEFAULT_STATELESS_NAME)))

        val tempAddress = getNewAddress()
        val config = LoadBalancer(
            name = VirtualMachineName(LOAD_BALANCER_NAME),
            address = tempAddress,
            workers = getWorkers(),
        )
        loadBalancerHandler = VmLifecycleHandlerImpl(
            initialConfig = config,
            manager = vmManager
        ) {
            if (configuration.master) {
                configuration.master = false
                val config = loadBalancerHandler.config as LoadBalancer
                loadBalancerHandler.config = config.copy(
                    address = getNewAddress(),
                )
                managerClient.requestNewMaster()
            }
        }
        loadBalancerHandler.createVirtualMachine()

        signalChange()
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
                address = getNewAddress(),
            ),
            manager = vmManager,
        )

        handler.createVirtualMachine()

        lifecycleHandlers[request.name] = handler

        reloadLoadBalancer()
        signalChange()
    }

    override fun deleteVirtualMachine(name: VirtualMachineName) {
        if (lifecycleHandlers.size == 1) {
            return
        }
        val handler = lifecycleHandlers[name]
        if (handler != null) {
            handler.deleteVirtualMachine()
            lifecycleHandlers.remove(name)
            availableAddresses.add(handler.config.address)
            reloadLoadBalancer()
            signalChange()
        }
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
        reloadLoadBalancer()
    }

    override fun becomeMaster() {
        configuration.master = true
        val config = loadBalancerHandler.config as LoadBalancer
        val currentAddress = config.address
        loadBalancerHandler.updateConfigAndRecreate(
            config.copy(
                address = configuration.publicAddress!!,
            )
        )
        availableAddresses.add(currentAddress)
    }

    private fun reloadLoadBalancer() {
        val config = loadBalancerHandler.config as LoadBalancer
        loadBalancerHandler.updateConfigAndRecreate(
            config.copy(
                workers = getWorkers(),
            )
        )
    }

    private fun signalChange() {
        val workers = lifecycleHandlers.values.map { it.config }
        remoteManagersView.signalConfigurationChange(workers)
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
        const val DEFAULT_STATELESS_NAME = "default"
    }
}
