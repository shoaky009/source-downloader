package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.DynamicTrigger
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object DynamicTriggerSupplier : SdComponentSupplier<DynamicTrigger> {
    override fun apply(props: ComponentProps): DynamicTrigger {
        return DynamicTrigger(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trigger("dynamic"))
    }

    override fun getComponentClass(): Class<DynamicTrigger> {
        return DynamicTrigger::class.java
    }

}