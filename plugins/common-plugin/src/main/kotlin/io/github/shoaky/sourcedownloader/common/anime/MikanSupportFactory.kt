package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties

object MikanSupportFactory : InstanceFactory<MikanClient> {

    override fun create(props: Properties): MikanClient {
        return MikanClient(
            props.getOrNull<String>("token")
        )
    }

    override fun type(): Class<MikanClient> {
        return MikanClient::class.java
    }
}