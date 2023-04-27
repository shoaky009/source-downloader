package xyz.shoaky.sourcedownloader.component.supplier

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import xyz.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

@Component
class WebhookTriggerSupplier(
    private val requestMapping: RequestMappingHandlerMapping
) : SdComponentSupplier<WebhookTrigger> {
    override fun apply(props: Properties): WebhookTrigger {
        return WebhookTrigger(props.get("path"), props.getOrDefault("method", "GET"), requestMapping)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("webhook"))
    }

}