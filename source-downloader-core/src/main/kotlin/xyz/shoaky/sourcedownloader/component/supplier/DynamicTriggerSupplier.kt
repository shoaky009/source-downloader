package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.trigger.DynamicTrigger
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object DynamicTriggerSupplier : SdComponentSupplier<DynamicTrigger> {
    override fun apply(props: Properties): DynamicTrigger {
        return DynamicTrigger(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("dynamic"))
    }

}