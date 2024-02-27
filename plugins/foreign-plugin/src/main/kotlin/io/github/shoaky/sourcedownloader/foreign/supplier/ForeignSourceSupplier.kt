package io.github.shoaky.sourcedownloader.foreign.supplier

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.foreign.component.ForeignSource
import io.github.shoaky.sourcedownloader.foreign.http.HttpForeignStateClient
import io.github.shoaky.sourcedownloader.foreign.methods.SourceForeignMethods
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.net.URI

object ForeignSourceSupplier : ComponentSupplier<ForeignSource> {

    override fun apply(context: CoreContext, props: Properties): ForeignSource {
        val client = createClient(props)
        val methods = props.getOrDefault("methods", SourceForeignMethods())
        return ForeignSource(client, methods)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("foreign"),
            ComponentType.source("remote"),
        )
    }

    private fun createClient(props: Properties): ForeignStateClient {
        val type = props.get<String>("type")
        when (type) {
            "http" -> {
                val server = props.get<URI>("server")
                val authorization = props.getOrNull<String>("authorization")
                return HttpForeignStateClient(server, authorization)
            }

            "grpc" -> {

            }

            "native" -> {

            }

        }
        throw ComponentException("Unknown source type: $type", "unknown-type")
    }
}