package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Component
class WebhookTriggerSupplier(
    @Qualifier("requestMappingHandlerMapping")
    private val requestMapping: RequestMappingHandlerMapping
) : ComponentSupplier<WebhookTrigger> {

    override fun apply(context: CoreContext, props: Properties): WebhookTrigger {
        return WebhookTrigger(props.get("path"), props.getOrDefault("method", "GET"), requestMapping)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("webhook"))
    }

}