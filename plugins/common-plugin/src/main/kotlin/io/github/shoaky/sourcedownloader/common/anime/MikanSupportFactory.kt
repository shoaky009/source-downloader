package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object MikanSupportFactory : InstanceFactory<MikanClient> {

    override fun create(props: Properties): MikanClient {
        return MikanClient(
            props.getOrNull<String>("token")
        )
    }

    override fun type(): Class<MikanClient> {
        return MikanClient::class.java
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            description = "Mikan 客户端实例",
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "token" to JsonSchema(type = "string", title = "Token"),
                ),
            )
        )
    }
}