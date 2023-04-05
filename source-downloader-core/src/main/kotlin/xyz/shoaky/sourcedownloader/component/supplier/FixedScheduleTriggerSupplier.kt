package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.FixedScheduleTrigger
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Trigger
import java.time.Duration

object FixedScheduleTriggerSupplier : SdComponentSupplier<FixedScheduleTrigger> {
    override fun apply(props: ComponentProps): FixedScheduleTrigger {
        val interval = props.properties["interval"]?.toString()
            ?: throw IllegalArgumentException("interval is required")
        val onStartRunTasks = props.properties["on-start-run-tasks"]?.toString()
        return FixedScheduleTrigger(Duration.parse(interval), "true" == onStartRunTasks)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("fixed", Trigger::class))
    }

    override fun getComponentClass(): Class<FixedScheduleTrigger> {
        return FixedScheduleTrigger::class.java
    }

}