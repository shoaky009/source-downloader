package io.github.shoaky.sourcedownloader.common.anime

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.bangumi.SearchSubjectV0Request
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
        val body = bgmTvApiClient.execute(
            SearchSubjectV0Request(title)
        ).body()
        val subjectItem = body.data.firstOrNull()
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

    override fun extractFrom(text: String): String? {
        return searchCache.get(text).nativeName
    }

    companion object {

        private val log = LoggerFactory.getLogger(BgmTvVariableProvider::class.java)
    }
}