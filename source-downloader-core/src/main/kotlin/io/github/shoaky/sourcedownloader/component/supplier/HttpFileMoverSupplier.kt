package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.HttpFileMover
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object HttpFileMoverSupplier : SdComponentSupplier<HttpFileMover> {
    override fun apply(props: Properties): HttpFileMover {
        return HttpFileMover(
            props.get("server-url"),
            props.getOrNull("username"),
            props.getOrNull("password"),
            props.getOrDefault("delete-source", true),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("http"),
            ComponentType.fileMover("webdav"),
        )
    }

}