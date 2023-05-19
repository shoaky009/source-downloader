package xyz.shoaky.sourcedownloader.common.anime

import xyz.shoaky.sourcedownloader.external.tmdb.TmdbClientV2
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemGroup
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider

class AnimeVariableProvider(
    private val tmdbClientV2: TmdbClientV2
) : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        TODO("Not yet implemented")
    }

    override fun support(item: SourceItem): Boolean {
        TODO("Not yet implemented")
    }
}