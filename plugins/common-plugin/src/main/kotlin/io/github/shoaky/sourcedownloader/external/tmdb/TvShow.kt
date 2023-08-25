package io.github.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class TvShow(
    val id: Long,
    @JsonProperty("number_of_seasons")
    val numberOfSeasons: Int? = null,
    val seasons: List<TmdbSeason>
)
