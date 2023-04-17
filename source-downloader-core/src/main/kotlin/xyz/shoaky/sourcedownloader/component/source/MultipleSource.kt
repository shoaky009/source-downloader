package xyz.shoaky.sourcedownloader.component.source

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source

class MultipleSource(
    private vararg val sources: Source
) : Source {
    override fun fetch(): List<SourceItem> {
        return sources.flatMap { it.fetch() }
    }
}