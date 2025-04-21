package pl.edu.pw.ia.manager.infrastructure

import org.intellij.lang.annotations.Language
import org.libvirt.Connect
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.heartbeat.infrastructure.logger
import pl.edu.pw.ia.manager.domain.Retry
import pl.edu.pw.ia.manager.domain.VirtualMachineManager
import pl.edu.pw.ia.manager.domain.model.LoadBalancer
import pl.edu.pw.ia.manager.domain.model.Stateless
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import pl.edu.pw.ia.manager.infrastructure.util.VirtualMachineConfigHelper
import pl.edu.pw.ia.manager.infrastructure.util.runCommand
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

@Service
class VirtualMachineManagerImpl(
    val connect: Connect,
) : VirtualMachineManager {

    private val logger = logger()

    override fun deleteAllVirtualMachines() {
        val ids = connect.listDomains()
        val domains = ids.map { id -> connect.domainLookupByID(id) }
        domains.forEach { domain ->
            deleteVirtualMachine(VirtualMachineName(domain.name))
        }
    }

    override fun createVirtualMachine(config: VirtualMachineConfig) {
        prepareImage(config)
        setupMachine(config)
        setupNetwork(config)

        when (config) {
            is Stateless -> startService(config)
            is LoadBalancer -> {
                startHeartBeat(config)
                updateLoadBalancer(config)
            }
        }
    }

    override fun updateVirtualMachine(config: VirtualMachineConfig) {
        setupNetwork(config)
        if (config is LoadBalancer) {
            updateLoadBalancer(config)
        }
    }

    private fun prepareImage(config: VirtualMachineConfig) {
        val inputStream = this::class.java.getResourceAsStream("/images/${config.type.name.lowercase()}.qcow2")

        if (inputStream == null) {
            error("Cannot find image for config $config")
        }

        val targetPath = Path("$IMAGES_DIRECTORY/${config.name}.qcow2")
        Files.createDirectories(targetPath)
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun setupMachine(config: VirtualMachineConfig) {
        val xmlConfig = VirtualMachineConfigHelper.kvmConfiguration(config)
        val domain = connect.domainCreateXML(xmlConfig, 0)

        Retry.retryUntilTrue(interval = 1.seconds) { domain.isActive == VM_IS_RUNNING }

        @Language("Shell Script")
        val command = """
            virsh qemu-agent-command ${config.name} '{"execute": "guest-ping"}'
        """.trimIndent()

        Retry.retryUntilSuccess { command.runCommand(checked = false) }
    }

    private fun setupNetwork(config: VirtualMachineConfig) {
        val ip = Retry.retryUntilSuccess {
            runCatching { findIp(config.name) ?: error("Ip not available") }
        }.getOrThrow()

        runAnsiblePlaybook(
            playbook = "network.yaml",
            ipAddress = ip,
            configuration = mapOf(
                "current_ip" to ip.toString(),
                "new_ip" to config.address.ip.toString(),
            ),
        )

        @Language("Shell Script")
        val ping = """
            ping -c 1 ${config.address.ip}
        """.trimIndent()
        Retry.retryUntilSuccess(
            interval = 1.seconds,
            timeout = 20.seconds,
        ) {
            ping.runCommand(checked = false)
        }.onFailure {
            error("IP not reachable")
        }
    }

    private fun startHeartBeat(config: VirtualMachineConfig) {
        runAnsiblePlaybook(
            playbook = "heart_beat.yaml",
            ipAddress = config.address.ip,
            configuration = mapOf(
                "ip" to config.address.ip.toString(),
                "port" to config.address.port.toString(),
            )
        )
    }

    private fun updateLoadBalancer(config: LoadBalancer) {
        @Language("Nginx Configuration")
        val nginxConfig = VirtualMachineConfigHelper.nginxConfiguration(config.workers)

        File("$PLAYBOOK_CONFIG_DIRECTORY/nginx.conf").bufferedWriter()
            .use { writer ->
                writer.write(nginxConfig)
            }
        runAnsiblePlaybook(
            playbook = "load_balancer.yaml",
            ipAddress = config.address.ip,
            configuration = mapOf(
                "ip" to config.address.ip.toString(),
                "port" to config.address.port.toString(),
            )
        )
    }

    private fun startService(config: Stateless) {
        runAnsiblePlaybook(
            playbook = "stateless.yaml",
            ipAddress = config.address.ip,
            configuration = mapOf(
                "ip" to config.address.ip.toString(),
                "port" to config.address.port.toString(),
            )
        )
    }

    override fun deleteVirtualMachine(name: VirtualMachineName) {
        try {
            val domain = connect.domainLookupByName(name.value)
            domain.destroy()
            val imagePath = Path("$IMAGES_DIRECTORY/${name.value}.qcow2")
            Files.deleteIfExists(imagePath)
        } catch (exception: Exception) {
            error("Failed to delete VM: ${exception.message}")
        }
    }

    override fun findIp(name: VirtualMachineName): IpAddress? {
        @Language("Shell Script")
        val command = """
            virsh domifaddr $name --source agent
        """.trimIndent()

        val cliResult = command.runCommand()
            .getOrElse {
                logger.warn("Failed to get IP address for VM ${name.value}", it)
                return null
            }

        val match = IP_REGEX.find(cliResult.stdOut)

        return match?.value?.let(::IpAddress)
    }

    private fun runAnsiblePlaybook(
        playbook: String,
        ipAddress: IpAddress,
        configuration: Map<String, String> = mapOf(),
    ) {
        val variables = configuration.entries.joinToString(separator = " ") { (key, value) -> "$key=$value" }

        @Language("Shell Script")
        val command = """
            ansible-playbook -i $ipAddress $PLAYBOOK_DIRECTORY/$playbook -e $variables
        """.trimIndent()

        command.runCommand().onFailure { exc -> throw exc }
    }

    companion object {
        const val IMAGES_DIRECTORY: String = "./images"
        const val PLAYBOOK_DIRECTORY: String = "./playbooks"
        const val PLAYBOOK_CONFIG_DIRECTORY: String = "./playbooks/config"

        const val VM_IS_RUNNING: Int = 1
        const val VM_INACTIVE: Int = 0

        val IP_REGEX = """\b(\d{1,3}(?:\.\d{1,3}){3})\b""".toRegex()
    }
}
