package pl.edu.pw.ia.manager.infrastructure

import org.intellij.lang.annotations.Language
import org.libvirt.Connect
import org.springframework.stereotype.Service
import pl.edu.pw.ia.heartbeat.infrastructure.logger
import pl.edu.pw.ia.manager.domain.VirtualMachineManager
import pl.edu.pw.ia.manager.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
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
        @Language("XML")
        val xmlConfig = """
            <domain type='kvm'>
                <name>${config.name}</name>
                <memory unit='MiB'>512</memory>
                <vcpu>1</vcpu>
                <os>
                    <type arch='x86_64' machine='pc-i440fx-2.9'>hvm</type>
                    <boot dev='hd'/>
                </os>
                <devices>
                    <disk type='file' device='disk'>
                        <driver name='qemu' type='qcow2'/>
                        <source file='$IMAGES_DIRECTORY/${config.name}.qcow2'/>
                        <target dev='vda' bus='virtio'/>
                        <address type='pci' domain='0x0000' bus='0x00' slot='0x04' function='0x0'/>
                    </disk>
                    <interface type='bridge'>
                        <source bridge='virbr0'/>
                        <model type='virtio'/>
                        <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>
                    </interface>
                    <graphics type='vnc' port='-1' listen='0.0.0.0'/>
                    <channel type='unix'>
                        <target type='virtio' name='org.qemu.guest_agent.0'/>
                    </channel>
                </devices>
            </domain>
        """.trimIndent()

        val domain = connect.domainCreateXML(xmlConfig, 0)

        // TODO: handle infinite loop
        while (domain.isActive == 0) {
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
        TODO("Not yet implemented")
    }

    override fun findIp(name: VirtualMachineName): IpAddress? {
        TODO("Not yet implemented")
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
        const val PLAYBOOK_DIRECTORY: String = "./ansible"
    }
}
