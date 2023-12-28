package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties

interface ComponentSupplier<R : SdComponent> {

    /**
     * @param props component properties from storage
     * @throws ComponentException if props is invalid
     */
    fun apply(context: CoreContext, props: Properties): R

    /**
     * @return component types that this supplier can supply
     */
    fun supplyTypes(): List<ComponentType>

    /**
     * @return component rules
     */
    fun rules(): List<ComponentRule> = emptyList()

    /**
     * only no args constructor
     * @return true if component can be auto created, otherwise false
     */
    fun autoCreateDefault(): Boolean = false
}