package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties

object FanboxClientFactory : InstanceFactory<FanboxClient> {

    override fun create(props: Properties): FanboxClient {
        return FanboxClient(props.get("session-id"))
    }

    override fun type(): Class<FanboxClient> {
        return FanboxClient::class.java
    }
}