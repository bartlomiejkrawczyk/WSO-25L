package pl.edu.pw.ia.manager.application

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
import pl.edu.pw.ia.heartbeat.domain.model.IpAddress
import pl.edu.pw.ia.manager.domain.OrchestrationManager
import pl.edu.pw.ia.manager.domain.model.CreateMachine
import pl.edu.pw.ia.manager.domain.model.VirtualMachineName
import reactor.core.publisher.Mono

@Tag(name = "Manager")
@ApiResponse(
    responseCode = "500",
    description = "Internal Server Error."
)
@ApiResponse(responseCode = "200", description = "OK.")
interface ManagerController {

    @Operation(summary = "Create a machine with stateless service")
    fun createVirtualMachine(
        request: CreateMachine
    ): Mono<Void>

    @Operation(summary = "Delete machine with given name")
    fun deleteVirtualMachine(
        name: VirtualMachineName,
    ): Mono<Void>

    @Operation(summary = "Find ip of a machine")
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
