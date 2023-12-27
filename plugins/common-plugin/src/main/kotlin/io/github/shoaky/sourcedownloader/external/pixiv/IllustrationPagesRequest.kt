package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import java.net.URI

class IllustrationPagesRequest(
    illustrationId: Long
) : BaseRequest<PixivResponse<List<Image>>>() {

    override val path: String = "/ajax/illust/$illustrationId/pages"
    override val responseBodyType: TypeReference<PixivResponse<List<Image>>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null
}

data class Image(
    val urls: Map<String, URI>,
    val width: Int,
    val height: Int
)