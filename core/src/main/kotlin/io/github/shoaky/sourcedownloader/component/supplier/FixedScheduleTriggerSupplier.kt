package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.trigger.FixedScheduleTrigger
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.Trigger
import java.time.Duration

object FixedScheduleTriggerSupplier : ComponentSupplier<FixedScheduleTrigger> {

    override fun apply(props: Properties): FixedScheduleTrigger {
        val interval = props.get<Duration>("interval")
        val onStartRunTasks = props.getOrDefault("on-start-run-tasks", false)
        return FixedScheduleTrigger(interval, onStartRunTasks)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("fixed", Trigger::class))
    }

}