package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.sdk.util.Http
import java.net.URLEncoder
import java.net.http.HttpRequest

class TmdbClientImpl(
    private val defaultApiKey: String
) : TmdbClient {

    override fun getTvShow(tvId: Long, language: String, apiKey: String?): TvShow {
        return invoke(
            "https://api.themoviedb.org/3/tv/$tvId?language=$language",
            apiKey,
            jacksonTypeRef()
        )
    }

    override fun searchTvShow(text: String, language: String, apiKey: String?): List<PageResult> {
        val encode = URLEncoder.encode(text, Charsets.UTF_8)
        return invoke(
            "https://api.themoviedb.org/3/search/tv?language=$language&page=1&query=$encode&include_adult=true",
            apiKey,
            jacksonTypeRef()
        )
    }

    private fun <T : Any> invoke(uri: String, apiKey: String? = null, typeReference: TypeReference<T>): T {
        val build = UriComponentsBuilder.fromUriString(uri)
            .queryParam("api_key", apiKey ?: defaultApiKey)
            .build()

        val client = Http.client
        val request = HttpRequest.newBuilder()
            .uri(build.toUri())
            .build()
        return client.send(request, Http.CommonBodyHandler(typeReference)).body()
    }

}