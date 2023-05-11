package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.HttpFileMover
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object HttpFileMoverSupplier : SdComponentSupplier<HttpFileMover> {
    override fun apply(props: Properties): HttpFileMover {
        return HttpFileMover(
            props.get("server-url"),
            props.getOrNull("username"),
            props.getOrNull("password"),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("http"),
            ComponentType.fileMover("webdav"),
        )
    }

}