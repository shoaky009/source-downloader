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
     * If component is not defined in storage, can it be apply with [Properties.empty]
     * @return true if component can be apply [Properties.empty], otherwise false
     */
    fun supportNoArgs(): Boolean = false

    /**
     *
     * @return component metadata
     */
    fun metadata(): ComponentMetadata? = null
}
