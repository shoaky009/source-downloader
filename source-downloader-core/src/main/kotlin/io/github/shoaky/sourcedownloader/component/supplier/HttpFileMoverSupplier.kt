package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.HttpFileMover
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object HttpFileMoverSupplier : ComponentSupplier<HttpFileMover> {

    override fun apply(props: Properties): HttpFileMover {
        return HttpFileMover(
            props.get("url"),
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