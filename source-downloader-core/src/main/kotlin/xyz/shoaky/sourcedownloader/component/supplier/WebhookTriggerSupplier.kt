package xyz.shoaky.sourcedownloader.component.supplier

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import xyz.shoaky.sourcedownloader.component.WebhookTrigger
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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