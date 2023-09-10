package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class PostTagRequest(
    campaignId: Long,
) : PatreonRequest<PatreonResponse<List<PatreonEntity<PostTag>>>>() {

    override val path: String = "/api/campaigns/$campaignId/post-tags"
    override val responseBodyType: TypeReference<PatreonResponse<List<PatreonEntity<PostTag>>>> = jacksonTypeRef()

}

data class PostTag(
    @JsonProperty("tag_type")
    val tagType: String,
    val value: String
)

