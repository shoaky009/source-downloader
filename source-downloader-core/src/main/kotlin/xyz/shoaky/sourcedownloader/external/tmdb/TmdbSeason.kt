package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class TmdbSeason(
    val name: String,
    @JsonProperty("season_number")
    val seasonNumber: Int,
)
