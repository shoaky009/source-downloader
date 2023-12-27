package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import java.net.URI

class GetUgoiraMetaRequest(
    illustrationId: Long
) : BaseRequest<PixivResponse<Ugoira>>() {

    override val path: String = "/ajax/illust/$illustrationId/ugoira_meta"
    override val responseBodyType: TypeReference<PixivResponse<Ugoira>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null
}

data class Ugoira(
    val src: URI,
    val originalSrc: URI,
    @JsonProperty("mime_type")
    val mimeType: String,
)