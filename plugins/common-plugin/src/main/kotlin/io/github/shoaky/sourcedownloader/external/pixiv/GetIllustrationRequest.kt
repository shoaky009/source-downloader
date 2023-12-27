package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class GetIllustrationRequest(
    val id: Long
) : BaseRequest<PixivResponse<IllustrationDetail>>() {

    override val path: String = "/ajax/illust/$id"
    override val responseBodyType: TypeReference<PixivResponse<IllustrationDetail>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null

}