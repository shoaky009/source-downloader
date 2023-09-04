package io.github.shoaky.sourcedownloader.sdk.component

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourcePointer

/**
 * A template source that fetches items from oldest to latest.
 *
 * 1.[requestItems]需要注意该target何时结束, 一般情况是已经拉取到最新的数据不能继续往下了
 * 2.能够从某些条件从上一次的状态继续获取新数据, 比如游标或者日期的条件查询等等
 * 3.当[requestItems]重复获取最新的数据需要过滤已经获取过的数据, 比如用日期或自增的id过滤等
 */
abstract class OldestToLatestSource<T, SP : SourcePointer> : Source<SP> {

    override fun fetch(pointer: SP, limit: Int): Iterable<PointedItem<ItemPointer>> {
        val targets = targets(pointer)
        val latestToIterator = OldestToLatestIterator(targets, limit) {
            requestItems(pointer, it)
        }
        return latestToIterator.asSequence().filterNotNull().asIterable()
    }

    abstract fun targets(pointer: SP): List<T>

    abstract fun requestItems(pointer: SP, target: T): RequestResult<PointedItem<ItemPointer>>

}

private class OldestToLatestIterator<T, R>(
    private val targets: List<T>,
    private val limit: Int,
    private val transform: (T) -> RequestResult<R>,
) : Iterator<R?> {

    private var targetIndex = 0
    private var counting = 0
    private var itemIndex = 0
    private var currentItems: List<R> = emptyList()

    override fun hasNext(): Boolean {
        val currDone = itemIndex >= currentItems.size
        if (!currDone) {
            return true
        }
        if (counting >= limit) {
            return false
        }
        return targets.getOrNull(targetIndex) != null
    }

    override fun next(): R? {
        if (itemIndex < currentItems.size) {
            counting++
            return currentItems[itemIndex++]
        }

        val target = targets[targetIndex]
        val result = transform.invoke(target)
        currentItems = result.items
        itemIndex = 0
        if (result.terminated) {
            targetIndex++
        }
        val item = currentItems.getOrNull(itemIndex++)
        if (item != null) {
            counting++
        }
        return item
    }
}

data class RequestResult<R>(
    val items: List<R>,
    val terminated: Boolean = false,
)