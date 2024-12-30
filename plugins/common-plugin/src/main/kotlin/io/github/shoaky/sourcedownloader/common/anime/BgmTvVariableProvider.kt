package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.SearchSubjectRequest
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import org.slf4j.LoggerFactory

class BgmTvVariableProvider(
    private val bgmTvApiClient: BgmTvApiClient = BgmTvApiClient()
) : VariableProvider {

    private val searchCache =
        CacheBuilder.newBuilder().maximumSize(500).build(object : CacheLoader<String, Anime>() {
            override fun load(title: String): Anime {
                return searchAnime(title)
            }
        })

    private fun searchAnime(title: String): Anime {
        if (title.isBlank()) {
            return Anime()
        }
        val body = bgmTvApiClient.execute(
            SearchSubjectRequest(title)
        ).body()
        val subjectItem = body.list.firstOrNull()
        if (subjectItem == null) {
            log.warn("bgmtv searching anime: $title no result")
            return Anime()
        }
        return Anime(
            nativeName = subjectItem.name
        )
    }

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val title = AnimeVariableProvider.extractTitle(sourceItem)
        return searchCache.get(title)
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables? {
        return searchCache.get(text)
    }

    override fun primary(): String {
        return "nativeName"
    }

    companion object {

        private val log = LoggerFactory.getLogger(BgmTvVariableProvider::class.java)
    }
}