package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.NullPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem

/**
 * Fetch latest items every time, no need to store pointer
 */
abstract class AlwaysLatestSource : Source<NullPointer> {

    final override fun fetch(
        pointer: NullPointer,
        limit: Int
    ): Iterable<PointedItem<ItemPointer>> {
        return fetch().map {
            PointedItem(it, NullPointer)
        }
    }

    abstract fun fetch(): Iterable<SourceItem>

    override fun defaultPointer(): NullPointer {
        return NullPointer
    }
}