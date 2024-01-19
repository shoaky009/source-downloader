package io.github.shoaky.sourcedownloader.external.ydl

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import java.net.URL

class SubmitDownload(
    val url: URL,
    val type: String = "video",
) : BaseRequest<Any>() {

    override val path: String = "/api/downloadFile"
    override val responseBodyType: TypeReference<Any> =
        object : TypeReference<Any>() {}
    override val httpMethod: String = "POST"
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}