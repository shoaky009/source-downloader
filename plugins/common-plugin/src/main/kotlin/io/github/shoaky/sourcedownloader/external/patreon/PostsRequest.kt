package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.JoinStringSerializer
import java.time.YearMonth

class PostsRequest(
    @param:JsonProperty("filter[campaign_id]")
    val campaignId: Long,
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val include: List<String> = defaultInclude,
    @param:JsonProperty("filter[contains_exclusive_posts]")
    val containsExclusivePosts: Boolean = true,
    @param:JsonProperty("filter[is_draft]")
    val isDraft: Boolean = false,
    @param:JsonProperty("fields[post]")
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val post: List<String> = defaultPostFields,
    @param:JsonProperty("fields[media]")
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val media: List<String> = listOf("id", "image_urls", "download_url", "metadata", "file_name"),
    @param:JsonProperty("fields[campaign]")
    @param:JsonSerialize(using = JoinStringSerializer::class)
    val campaignFields: List<String> = listOf("creation_name", "pay_per_name", "one_liner"),
    val sort: String = "published_at",
    @param:JsonProperty("page[cursor]")
    val cursor: String? = null,
    @param:JsonProperty("filter[month]")
    @param:JsonFormat(pattern = "yyyy-MM")
    val month: YearMonth? = null,
) : PatreonRequest<PostsResponse>() {

    companion object {

        private val defaultInclude = listOf(
            "campaign", "user"
        )
        private val defaultPostFields = listOf(
            "content", "current_user_can_view", "embed", "image", "is_paid",
            "meta_image_url", "post_file", "published_at", "patreon_url", "post_type", "preview_asset_type",
            "thumbnail", "thumbnail_url", "title", "url"
        )
    }

    override val path: String = "/api/posts"
    override val responseBodyType: TypeReference<PostsResponse> = jacksonTypeRef()

}