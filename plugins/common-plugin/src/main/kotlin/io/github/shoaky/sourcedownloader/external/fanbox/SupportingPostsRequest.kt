package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class SupportingPostsRequest(
    val limit: Int = 20,
) : FanboxRequest<Posts>() {

    override val path: String = "/post.listSupporting"
    override val responseBodyType: TypeReference<FanboxResponse<Posts>> = jacksonTypeRef()

}