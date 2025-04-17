package pl.edu.pw.ia.manager.infrastructure

import org.libvirt.Connect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectConfiguration {

    @Bean(destroyMethod = "close")
    fun connect(): Connect = Connect("qemu:///system")
}
