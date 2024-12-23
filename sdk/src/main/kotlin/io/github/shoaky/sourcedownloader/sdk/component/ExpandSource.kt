package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.IterationResult

/**
 * A template source that fetches items.
 *
 * 1.[requestItems]需要注意该target何时结束, 一般情况是已经拉取到最新的数据不能继续往下了
 * 2.能够从某些条件从上一次的状态继续获取新数据, 比如游标或者日期的条件查询等等
 * 3.当[requestItems]重复获取最新的数据需要过滤已经获取过的数据, 比如用日期或自增的id过滤等
 */
abstract class ExpandSource<T, SP : SourcePointer> : Source<SP> {

    override fun fetch(pointer: SP, limit: Int): Iterable<PointedItem<ItemPointer>> {
        val targets = targets(pointer)
        val context = FetchContext(pointer, limit)

        val latestToIterator = ExpandIterator(targets, limit) {
            requestItems(context, it)
        }
        return latestToIterator.asIterable()
    }

    abstract fun targets(pointer: SP): List<T>

    abstract fun requestItems(ctx: FetchContext<SP>, target: T): IterationResult<PointedItem<ItemPointer>>

}