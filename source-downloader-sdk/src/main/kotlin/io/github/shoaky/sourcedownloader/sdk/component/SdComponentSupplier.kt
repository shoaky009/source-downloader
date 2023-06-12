package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.Properties

interface SdComponentSupplier<R : SdComponent> {

    fun apply(props: Properties): R

    fun supplyTypes(): List<ComponentType>

    fun rules(): List<ComponentRule> = emptyList()

    /**
     * only no args constructor
     */
    fun autoCreateDefault(): Boolean = false
}