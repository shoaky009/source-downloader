package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.WebdavFileMover
import io.github.shoaky.sourcedownloader.external.webdav.WebdavClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.net.URI

object WebdavMoverSupplier : ComponentSupplier<WebdavFileMover> {

    override fun apply(props: Properties): WebdavFileMover {
        val username = props.getOrNull<String>("username")
        val server = props.get<URI>("server")
        val password = props.getOrNull<String>("password")
        return WebdavFileMover(
            WebdavClient(server, username, password),
            props.getOrDefault("delete-source", true),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileMover("webdav"),
        )
    }

}