package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class HomePostsRequest(
    val limit: Int = 20,
) : FanboxRequest<Posts>() {

    override val path: String = "/post.listHome"
    override val responseBodyType: TypeReference<FanboxResponse<Posts>> = jacksonTypeRef()
}