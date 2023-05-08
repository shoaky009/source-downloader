package xyz.shoaky.sourcedownloader.sdk.component

import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.Properties

interface SdComponentSupplier<R : SdComponent> {

    fun apply(props: Properties): R
    fun supplyTypes(): List<ComponentType>

    fun rules(): List<ComponentRule> = emptyList()

    /**
     * only no args constructor
     */
    fun autoCreateDefault(): Boolean = false
}