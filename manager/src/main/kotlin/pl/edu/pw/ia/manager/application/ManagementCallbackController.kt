package pl.edu.pw.ia.manager.application

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.pw.ia.heartbeat.domain.model.Address
import pl.edu.pw.ia.manager.domain.OrchestrationManager
import pl.edu.pw.ia.manager.domain.RemoteManagersView
import pl.edu.pw.ia.manager.domain.model.VirtualMachineConfig
import reactor.core.publisher.Flux

@Tag(name = "Callback")
@ApiResponse(
    responseCode = "500",
    description = "Internal Server Error."
)
@ApiResponse(responseCode = "200", description = "OK.")
interface ManagementCallbackController {

    @Operation(summary = "List virtual machines assigned to given manager")
    fun listVirtualMachines(): Flux<VirtualMachineConfig>

    @Operation(summary = "Callback for signalling to other managers vm configuration change")
    fun configurationChanged(manager: Address, configs: Collection<VirtualMachineConfig>)
}

@RestController
@RequestMapping("/callback", produces = [MediaType.APPLICATION_JSON_VALUE])
class ManagementCallbackControllerImpl(
    private val orchestrationManager: OrchestrationManager,
    private val remoteManagersView: RemoteManagersView,
) : ManagementCallbackController {

    @GetMapping
    override fun listVirtualMachines(): Flux<VirtualMachineConfig> {
        return Flux.fromIterable(orchestrationManager.listVirtualMachines())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    override fun configurationChanged(@RequestParam manager: Address, @RequestBody configs: Collection<VirtualMachineConfig>) {
        remoteManagersView.registerConfigurationChanged(manager, configs)
    }
}
