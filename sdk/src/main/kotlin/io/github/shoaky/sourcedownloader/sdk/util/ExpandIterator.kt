package io.github.shoaky.sourcedownloader.sdk.util

/**
 * A iterator that can expand the target to items
 * @param T the target type
 * @param R the item type
 * @param targets the targets to be expanded
 * @param limit the limit of total items, it may be more than the limit
 * @param transform the transform function from target to items
 * @return the iterator of items
 */
class ExpandIterator<T, R>(
    private val targets: List<T>,
    private val limit: Int,
    private val transform: (T) -> IterationResult<R>,
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

    fun toList(): List<R> {
        return asSequence().filterNotNull().toList()
    }

    fun asIterable(): Iterable<R> {
        return asSequence().filterNotNull().asIterable()
    }
}

/**
 * @param terminated if the target is terminated, goes to next target
 */
data class IterationResult<R>(
    val items: List<R>,
    val terminated: Boolean = false,
)