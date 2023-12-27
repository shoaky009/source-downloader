package io.github.shoaky.sourcedownloader.external.fanbox

import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod

abstract class FanboxRequest<T> : BaseRequest<FanboxResponse<T>>() {

    override val httpMethod: String = HttpMethod.GET.name
    override val mediaType: MediaType? = null
}