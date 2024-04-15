package io.github.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.http.HttpMethod

data class GetSeasonDetail(
    @JsonIgnore
    val seriesId: Long,
    @JsonIgnore
    val seasonNumber: Int,
    val language: String = "ja-jp",
) : BaseRequest<SeasonDetail>() {

    override val path: String = "/3/tv/$seriesId/season/$seasonNumber"
    override val responseBodyType: TypeReference<SeasonDetail> = jacksonTypeRef()
    override val httpMethod: String = HttpMethod.GET.name
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}

data class SeasonDetail(
    val id: Long,
    val name: String,
    @JsonProperty("season_number")
    val seasonNumber: Int,
    val episodes: List<Episode>
)

data class Episode(
    val id: Long,
    val name: String,
    @JsonProperty("episode_number")
    val episodeNumber: Int
)