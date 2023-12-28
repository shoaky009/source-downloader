package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.trigger.CronTrigger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object CronTriggerSupplier : ComponentSupplier<CronTrigger> {

    override fun apply(context: CoreContext, props: Properties): CronTrigger {
        return CronTrigger(props.get("expression"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.trigger("cron")
        )
    }
}