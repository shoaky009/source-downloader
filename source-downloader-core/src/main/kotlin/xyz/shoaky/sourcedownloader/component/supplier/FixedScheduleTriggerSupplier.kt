package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.trigger.FixedScheduleTrigger
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.time.Duration

object FixedScheduleTriggerSupplier : SdComponentSupplier<FixedScheduleTrigger> {
    override fun apply(props: ComponentProps): FixedScheduleTrigger {
        val interval = props.properties["interval"]?.toString()
            ?: throw ComponentException.props("interval is required")
        val onStartRunTasks = props.properties["on-start-run-tasks"]?.toString()
        return FixedScheduleTrigger(Duration.parse(interval), "true" == onStartRunTasks)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("fixed", Trigger::class))
    }

}