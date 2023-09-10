package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.JoinStringSerializer
import java.time.YearMonth

class PostsRequest(
    @JsonProperty("filter[campaign_id]")
    val campaignId: Long,
    @JsonSerialize(using = JoinStringSerializer::class)
    val include: List<String> = defaultInclude,
    @JsonProperty("filter[contains_exclusive_posts]")
    val containsExclusivePosts: Boolean = true,
    @JsonProperty("filter[is_draft]")
    val isDraft: Boolean = false,
    @JsonProperty("fields[post]")
    @JsonSerialize(using = JoinStringSerializer::class)
    val post: List<String> = defaultPostFields,
    @JsonProperty("fields[media]")
    @JsonSerialize(using = JoinStringSerializer::class)
    val media: List<String> = listOf("id", "image_urls", "download_url", "metadata", "file_name"),
    @JsonProperty("fields[campaign]")
    @JsonSerialize(using = JoinStringSerializer::class)
    val campaignFields: List<String> = listOf("creation_name", "pay_per_name", "one_liner"),
    val sort: String = "published_at",
    @JsonProperty("page[cursor]")
    val cursor: String? = null,
    @JsonProperty("filter[month]")
    @JsonFormat(pattern = "yyyy-MM")
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