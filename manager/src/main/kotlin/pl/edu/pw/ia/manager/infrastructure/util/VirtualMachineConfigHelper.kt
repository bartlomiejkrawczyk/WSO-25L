package pl.edu.pw.ia.manager.infrastructure.util

import org.intellij.lang.annotations.Language
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import pl.edu.pw.ia.manager.infrastructure.VirtualMachineManagerImpl.Companion.TEMP_IMAGES_DIRECTORY

object VirtualMachineConfigHelper {

    fun kvmConfiguration(config: VirtualMachineConfig): String {
        @Language("XML")
        val kvmConfig = """
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
                        <source file='$TEMP_IMAGES_DIRECTORY/${config.name}.qcow2'/>
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

        return kvmConfig
    }

    fun nginxConfiguration(workers: Collection<Address>): String {
        val port = workers.map { it.port }.first()
        val ips = workers.map { it.ip }
        val servers = ips.joinToString(separator = "\n\t\t") { ip -> "server $ip;" }

        @Language("Nginx Configuration")
        val nginxConfig = """
            user nginx;
            
            worker_processes auto;
            
            pcre_jit on;
            
            error_log /var/log/nginx/error.log warn;
            include /etc/nginx/modules/*.conf;
            include /etc/nginx/conf.d/*.conf;
            
            events {
                worker_connections 1024;
            }
            
            http {
                limit_req_zone ${'$'}binary_remote_addr zone=limit:1m rate=1r/s;
                
                upstream workers {
                    $servers
                }
                
                server {
                    listen $port;
                    
                    location / {
                        limit_req zone=limit;
                        proxy_pass http://workers;
                    }
                }
            }
        """.trimIndent()

        return nginxConfig
    }
}
