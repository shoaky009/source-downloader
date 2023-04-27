package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.SendHttpRequest
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

object SendHttpRequestSupplier : SdComponentSupplier<SendHttpRequest> {
    override fun apply(props: Properties): SendHttpRequest {
        return SendHttpRequest(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.run("http")
        )
    }

}