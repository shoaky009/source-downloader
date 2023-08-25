package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
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
}