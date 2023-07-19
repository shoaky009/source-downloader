package io.github.shoaky.sourcedownloader.sdk.component

import com.google.common.collect.Iterables
import io.github.shoaky.sourcedownloader.sdk.NullPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem

/**
 * 根源没有时间 limit等请求参数
 */
abstract class AlwaysLatestSource : Source<NullPointer> {

    final override fun fetch(
        pointer: NullPointer?,
        limit: Int
    ): Iterable<PointedItem<NullPointer>> {
        return Iterables.transform(fetch()) {
            PointedItem(
                it,
                NullPointer
            )
        }
    }

    abstract fun fetch(): Iterable<SourceItem>
}