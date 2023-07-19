package io.github.shoaky.sourcedownloader.sdk.util

/**
 * Expend [expendItems] until [limit] is reached.
 * The total expended items may be bigger than [limit] all depends on [transform].
 * [T] items to expend, [R] expended items
 */
class LimitedExpander<T, R>(
    private val expendItems: List<T>,
    private val limit: Int,
    private val transform: (T) -> List<R>,
) : Iterator<List<R>> {

    private var counting = 0
    private val expandIterator: Iterator<List<R>> = expendItems.map { transform.invoke(it) }.iterator()

    override fun hasNext(): Boolean {
        if (expandIterator.hasNext().not()) {
            return false
        }
        return counting < limit
    }

    override fun next(): List<R> {
        val next = expandIterator.next()
        counting += next.size
        return next
    }

}