package xyz.shoaky.sourcedownloader.external.tmdb

interface TmdbClient {

    fun getTvShow(tvId: Long, language: String = "jpn", apiKey: String? = null): TvShow?

    fun searchTvShow(text: String, language: String = "jpn", apiKey: String? = null): List<PageResult>

    companion object {
        fun create(apiKey: String = System.getenv("TMDB_APIKEY_V3_AUTH")
            ?: "7d82a6a830d5f4458f42929f73878195"): TmdbClient {
            return TmdbClientImpl(apiKey)
        }
    }
}