package pl.edu.pw.ia.manager.application

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.ia.heartbeat.application.ValueWrapper
import pl.edu.pw.ia.manager.domain.OrchestrationManager
import pl.edu.pw.ia.manager.domain.model.CreateMachine
import pl.edu.pw.ia.manager.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import reactor.core.publisher.Mono

interface ManagerController {

    fun createVirtualMachine(
        request: CreateMachine
    ): Mono<Void>

    fun deleteVirtualMachine(
        name: VirtualMachineName,
    ): Mono<Void>

    fun findIp(
        name: VirtualMachineName,
    ): Mono<ValueWrapper<IpAddress>>
}

@RestController
@RequestMapping("/manager", produces = [MediaType.APPLICATION_JSON_VALUE])
class ManagerControllerImpl(
    private val manager: OrchestrationManager,
) : ManagerController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    override fun createVirtualMachine(
        @RequestBody request: CreateMachine
    ): Mono<Void> {
        manager.createVirtualMachine(request)
        return Mono.empty()
    }

    @DeleteMapping("/{name}")
    override fun deleteVirtualMachine(@PathVariable name: VirtualMachineName): Mono<Void> {
        manager.deleteVirtualMachine(name)
        return Mono.empty()
    }

    @GetMapping("/{name}")
    override fun findIp(@PathVariable name: VirtualMachineName): Mono<ValueWrapper<IpAddress>> =
        Mono.justOrEmpty<IpAddress>(manager.findIp(name)).map(::ValueWrapper)
}
