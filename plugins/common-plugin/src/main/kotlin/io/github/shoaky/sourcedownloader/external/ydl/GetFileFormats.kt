package io.github.shoaky.sourcedownloader.external.ydl

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import java.net.URL

class GetFileFormats(
    val url: URL,
) : BaseRequest<YoutubeDLResponse<FileFormate>>() {

    override val path: String = "/api/getFileFormats"
    override val responseBodyType: TypeReference<YoutubeDLResponse<FileFormate>> =
        object : TypeReference<YoutubeDLResponse<FileFormate>>() {}
    override val httpMethod: String = "POST"
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}