package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class GetUserAllRequest(
    userId: Long,
    val lang: String = "zh",
) : BaseRequest<PixivResponse<GetUserAll>>() {

    override val path: String = "/ajax/user/$userId/profile/all"
    override val responseBodyType: TypeReference<PixivResponse<GetUserAll>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null
}