package xyz.shoaky.sourcedownloader.core.component

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

class WebhookTrigger(
    private val path: String,
    private val method: String = "GET",
    private val requestMapping: RequestMappingHandlerMapping
) : TaskHolderTrigger() {
    override fun start() {
        val info = requestMappingInfo()
        requestMapping.registerMapping(info, this,
            WebhookTrigger::class.java.methods.first { it.name == "endpoint" })
    }

    @Suppress("UNUSED")
    fun endpoint(): ResponseEntity<Any> {
        for (task in tasks) {
            task.run()
        }
        return ResponseEntity.noContent().build()
    }

    private fun requestMappingInfo(): RequestMappingInfo {
        val options = RequestMappingInfo.BuilderConfiguration()
        options.patternParser = PathPatternParser()
        return RequestMappingInfo.paths("/webhook/$path")
            .methods(RequestMethod.valueOf(method))
            .produces(MediaType.APPLICATION_JSON_VALUE)
            .options(options)
            .build()
    }

    override fun stop() {
        requestMapping.unregisterMapping(requestMappingInfo())
    }

}

@Component
class WebhookTriggerSupplier(
    private val requestMapping: RequestMappingHandlerMapping
) : SdComponentSupplier<WebhookTrigger> {
    override fun apply(props: ComponentProps): WebhookTrigger {
        return WebhookTrigger(props.get("path"), props.getOrDefault("method", "GET"), requestMapping)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("webhook"))
    }

    override fun getComponentClass(): Class<WebhookTrigger> {
        return WebhookTrigger::class.java
    }

}