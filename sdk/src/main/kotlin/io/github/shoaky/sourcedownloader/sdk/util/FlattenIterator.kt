package io.github.shoaky.sourcedownloader.sdk.util

private class FlattenIterator<T>(
    private val iterator: Iterator<Collection<T>>,
) : Iterator<T> {

    private var items = iterator.next().iterator()

    override fun hasNext(): Boolean {
        if (items.hasNext()) {
            return true
        }
        if (iterator.hasNext()) {
            items = iterator.next().iterator()
            return hasNext()
        }
        return false
    }

    override fun next(): T {
        return items.next()
    }
}

fun <T> Iterator<Collection<T>>.flatten(): Iterator<T> {
    return FlattenIterator(this)
}