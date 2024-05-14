package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.tmdb.SearchResult
import io.github.shoaky.sourcedownloader.external.tmdb.SearchTvShow
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import java.util.*
import kotlin.jvm.optionals.getOrNull

class TmdbVariableProvider(
    private val client: TmdbClient = TmdbClient.default,
    private val language: String = "zh-CN",
) : VariableProvider {

    private val cache = CacheBuilder.newBuilder().maximumSize(500).build(
        object : CacheLoader<String, Optional<SearchResult>>() {
            override fun load(content: String): Optional<SearchResult> {
                val results = client.execute(SearchTvShow(content, language = language)).body().results
                return Optional.ofNullable(
                    results.firstOrNull()
                )
            }
        })

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val result = cache.get(sourceItem.title).getOrNull() ?: return PatternVariables.EMPTY
        return result.resultToVars()
    }

    override fun primary(): String {
        return "originalName"
    }

    override fun extractFrom(text: String): PatternVariables? {
        val result = cache.get(text).getOrNull()
        if (result != null) {
            return result.resultToVars()
        }

        val (first) = text.split(" ")
        return cache.get(first).getOrNull()?.resultToVars() ?: return null
    }

    companion object {

        private fun SearchResult.resultToVars(): PatternVariables {
            return MapPatternVariables(
                mapOf(
                    "tmdbId" to this.id,
                    "tmdbName" to this.name,
                    "originalName" to this.originalName,
                )
            )
        }
    }
}