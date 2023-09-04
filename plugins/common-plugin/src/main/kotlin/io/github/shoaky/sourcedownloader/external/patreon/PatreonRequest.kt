package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.util.http.CommonBodyHandler
import io.github.shoaky.sourcedownloader.sdk.util.http.JsonBodyMapper
import io.github.shoaky.sourcedownloader.sdk.util.http.WarpBodyMapper

abstract class PatreonRequest<T : Any>(
    @JsonProperty("json-api-version")
    val jsonApiVersion: String = "1.0"
) : BaseRequest<T>() {

    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null

    override fun bodyHandler(): CommonBodyHandler<T> {
        return CommonBodyHandler(responseBodyType).apply {
            addBodyMapper("vnd.api+json") { WarpBodyMapper(JsonBodyMapper()) }
        }
    }
}