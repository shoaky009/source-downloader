package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.trigger.FixedScheduleTrigger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.time.Duration

object FixedScheduleTriggerSupplier : ComponentSupplier<FixedScheduleTrigger> {

    override fun apply(context: CoreContext, props: Properties): FixedScheduleTrigger {
        val interval = props.get<Duration>("interval")
        val onStartRunTasks = props.getOrDefault("on-start-run-tasks", false)
        return FixedScheduleTrigger(interval, onStartRunTasks)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.trigger("fixed")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("interval"),
                properties = mapOf(
                    "interval" to JsonSchema(
                        type = "string",
                        description = "ISO-8601 Duration，例如 PT30M"
                    ),
                    "on-start-run-tasks" to JsonSchema(
                        type = "boolean",
                        default = false
                    )
                )
            )
        )
    }

}
