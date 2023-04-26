package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.HttpFileMover
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object HttpFileMoverSupplier : SdComponentSupplier<HttpFileMover> {
    override fun apply(props: ComponentProps): HttpFileMover {
        return HttpFileMover(
            props.get("server-url"),
            props.getNotRequired("username"),
            props.getNotRequired("password"),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("http"),
            ComponentType.fileMover("webdav"),
        )
    }

}