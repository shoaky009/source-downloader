package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.SendHttpRequest
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object SendHttpRequestSupplier : SdComponentSupplier<SendHttpRequest> {
    override fun apply(props: ComponentProps): SendHttpRequest {
        return SendHttpRequest(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.run("http")
        )
    }

}