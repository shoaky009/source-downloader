package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.WebdavFileMover
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object HttpFileMoverSupplier : ComponentSupplier<WebdavFileMover> {

    override fun apply(props: Properties): WebdavFileMover {
        return WebdavFileMover(
            props.get("server"),
            props.getOrNull("username"),
            props.getOrNull("password"),
            props.getOrDefault("delete-source", true),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("webdav"),
        )
    }

}