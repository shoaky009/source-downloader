package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest

class GetFollowingRequest(
    @JsonIgnore
    val userId: Long,
    val offset: Int = 0,
    val limit: Int = 50,
    val rest: String = "show",
) : BaseRequest<PixivResponse<GetFollowing>>() {

    override val path: String = "/ajax/user/$userId/following"
    override val responseBodyType: TypeReference<PixivResponse<GetFollowing>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null

    fun nextRequest(): GetFollowingRequest {
        return GetFollowingRequest(userId, offset + limit, limit, rest)
    }
}