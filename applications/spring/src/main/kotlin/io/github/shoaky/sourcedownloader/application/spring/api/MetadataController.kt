package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.sdk.component.ComponentRootType
import io.github.shoaky.sourcedownloader.service.ComponentCapabilityDetail
import io.github.shoaky.sourcedownloader.service.ComponentCapabilitySummary
import io.github.shoaky.sourcedownloader.service.ComponentRootTypeMetadata
import io.github.shoaky.sourcedownloader.service.InstanceCapabilityDetail
import io.github.shoaky.sourcedownloader.service.InstanceCapabilitySummary
import io.github.shoaky.sourcedownloader.service.MetadataService
import io.github.shoaky.sourcedownloader.service.ProcessorMetadata
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/metadata")
private class MetadataController(
    private val metadataService: MetadataService,
) {

    @GetMapping("/processor")
    fun getProcessorMetadata(): ProcessorMetadata {
        return metadataService.processorMetadata()
    }

    @GetMapping("/component/root-types")
    fun getComponentRootTypes(): List<ComponentRootTypeMetadata> {
        return metadataService.componentRootTypes()
    }

    @GetMapping("/component")
    fun getComponentCapabilities(): List<ComponentCapabilitySummary> {
        return metadataService.componentCapabilitySummaries()
    }

    @GetMapping("/component/{rootType}/{typeName}")
    fun getComponentCapability(
        @PathVariable rootType: ComponentRootType,
        @PathVariable typeName: String,
    ): ComponentCapabilityDetail? {
        return metadataService.componentCapability(rootType, typeName)
    }

    @GetMapping("/instance")
    fun getInstanceCapabilities(): List<InstanceCapabilitySummary> {
        return metadataService.instanceCapabilitySummaries()
    }

    @GetMapping("/instance/{type}")
    fun getInstanceCapability(
        @PathVariable type: String,
    ): InstanceCapabilityDetail? {
        return metadataService.instanceCapability(type)
    }
}
