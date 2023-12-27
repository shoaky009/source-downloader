package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

class GetIllustrationsRequest(
    userId: Long,
    @JsonIgnore
    val ids: List<Long>,
    val lang: String = "zh"
) : BaseRequest<PixivResponse<Map<Long, Illustration>>>() {

    override val path: String = "/ajax/user/$userId/illusts?ids[]=${ids.joinToString("&ids[]=")}"
    override val responseBodyType: TypeReference<PixivResponse<Map<Long, Illustration>>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null
}