package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.SendHttpRequest
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SendHttpRequestSupplier : ComponentSupplier<SendHttpRequest> {

    override fun apply(context: CoreContext, props: Properties): SendHttpRequest {
        return SendHttpRequest(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.listener("http")
        )
    }

}