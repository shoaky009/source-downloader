package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.patreon.PatreonIntegration
import io.github.shoaky.sourcedownloader.external.patreon.PatreonClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object PatreonIntegrationSupplier : ComponentSupplier<PatreonIntegration> {

    override fun apply(context: CoreContext, props: Properties): PatreonIntegration {
        val sid = props.get<String>("session-id")
        val headers = props.getOrNull<Map<String, String>>("headers")
        headers?.let {
            return PatreonIntegration(PatreonClient(sid, headers = it))
        }
        return PatreonIntegration(PatreonClient(sid))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("patreon"),
            ComponentType.fileResolver("patreon"),
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(PatreonIntegration::class)
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("session-id"),
                properties = mapOf(
                    "session-id" to JsonSchema(type = "string"),
                    "headers" to JsonSchema(
                        type = "object",
                        additionalProperties = JsonSchema(type = "string")
                    )
                )
            )
        )
    }
}
