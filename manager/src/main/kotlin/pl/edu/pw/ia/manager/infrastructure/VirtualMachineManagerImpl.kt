package pl.edu.pw.ia.manager.infrastructure

import org.intellij.lang.annotations.Language
import org.libvirt.Connect
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.infrastructure.logger
import pl.edu.pw.ia.manager.domain.VirtualMachineManager
import pl.edu.pw.ia.manager.domain.model.Address
import pl.edu.pw.ia.manager.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import pl.edu.pw.ia.manager.infrastructure.util.VirtualMachineConfigHelper
import pl.edu.pw.ia.manager.infrastructure.util.runCommand
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path

@Service
class VirtualMachineManagerImpl(
    val connect: Connect,
) : VirtualMachineManager {

    private val logger = logger()

    override fun listAvailableVirtualMachines(): Collection<VirtualMachineConfig> {
        val ids = connect.listDomains()
        val domains = ids.map { id -> connect.domainLookupByID(id) }
        val configs = domains.map { domain ->
            TODO()
        }
        TODO()
    }

    override fun createVirtualMachine(config: VirtualMachineConfig) {
        TODO("Not yet implemented")
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

        // TODO: handle infinite loop
        while (domain.isActive == VM_INACTIVE) {
            sleep(1000)
        }

        @Language("Shell Script")
        val command = """
            virsh qemu-agent-command ${config.name} '{"execute": "guest-ping"}'
        """.trimIndent()

        // TODO: handle infinite loop
        var retry = true
        while (retry) {
            command.runCommand(checked = false)
                .onSuccess { result ->
                    retry = false
                }
        }
    }

    private fun awaitNetwork(config: VirtualMachineConfig): IpAddress {
        // TODO: retry ip
        // TODO: run ansible playbook with network setup
        // TODO: setup new ip address from virtual machine config
        // TODO: ping ip until it responds
        TODO()
    }

    private fun startService(config: VirtualMachineConfig) {

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

    private fun updateNginxConfig(
        workers: List<Address>,
    ) {
        @Language("Nginx Configuration")
        val nginxConfig = VirtualMachineConfigHelper.nginxConfiguration(workers)
    }

    companion object {
        const val IMAGES_DIRECTORY: String = "./images"
        const val PLAYBOOK_DIRECTORY: String = "./ansible"

        const val VM_IS_RUNNING: Int = 1
        const val VM_INACTIVE: Int = 0

        val IP_REGEX = """\b(\d{1,3}(?:\.\d{1,3}){3})\b""".toRegex()
    }
}
