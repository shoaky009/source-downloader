package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import java.net.URI

data class CreatorPaginateRequest(
    val creatorId: String,
) : FanboxRequest<List<URI>>() {

    override val path: String = "/post.paginateCreator"
    override val responseBodyType: TypeReference<FanboxResponse<List<URI>>> = jacksonTypeRef()
}