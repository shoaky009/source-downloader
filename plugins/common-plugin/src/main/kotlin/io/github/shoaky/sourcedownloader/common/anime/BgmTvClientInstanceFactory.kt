package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.net.URI

object BgmTvClientInstanceFactory : InstanceFactory<BgmTvApiClient> {

    override fun create(props: Properties): BgmTvApiClient {
        val endpoint = props.getOrDefault<URI>("endpoint", URI("https://api.bgm.tv/"))
        return props.getOrNull<String>("token")
            ?.let {
                BgmTvApiClient(it, endpoint)
            } ?: BgmTvApiClient(endpoint = endpoint)
    }

    override fun type(): Class<BgmTvApiClient> {
        return BgmTvApiClient::class.java
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            description = "Bangumi.tv API 客户端实例",
            propertySchema = JsonSchema(
                type = "object",
                required = emptyList(),
                properties = mapOf(
                    "token" to JsonSchema(type = "string", title = "Access Token"),
                    "endpoint" to JsonSchema(
                        type = "string",
                        title = "Endpoint",
                        format = "uri",
                        default = "https://api.bgm.tv/",
                    ),
                ),
            )
        )
    }
}
