package io.github.shoaky.sourcedownloader.external.ydl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class CancelDownload(
    @JsonProperty("download_uid")
    val downloadUid: String,
) : BaseRequest<Any>() {

    override val path: String = "/api/cancelDownload"
    override val responseBodyType: TypeReference<Any> =
        object : TypeReference<Any>() {}
    override val httpMethod: String = "POST"
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}