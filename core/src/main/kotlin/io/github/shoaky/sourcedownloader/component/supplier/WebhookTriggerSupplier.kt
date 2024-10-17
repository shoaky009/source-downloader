package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class WebhookTriggerSupplier(
    private val adapter: WebhookTrigger.Adapter
) : ComponentSupplier<WebhookTrigger> {

    override fun apply(context: CoreContext, props: Properties): WebhookTrigger {
        return WebhookTrigger(props.get("path"), props.getOrDefault("method", "GET"), adapter)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("webhook"))
    }

}