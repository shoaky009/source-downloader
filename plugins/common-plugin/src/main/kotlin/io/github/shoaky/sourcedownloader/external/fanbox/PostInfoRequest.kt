package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

data class PostInfoRequest(
    val postId: String
) : FanboxRequest<PostDetail>() {
    
    override val path: String = "/post.info"
    override val responseBodyType: TypeReference<FanboxResponse<PostDetail>> = jacksonTypeRef()

}