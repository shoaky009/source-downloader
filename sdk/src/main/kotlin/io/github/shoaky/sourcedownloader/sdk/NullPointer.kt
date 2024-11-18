package io.github.shoaky.sourcedownloader.sdk

object NullPointer : SourcePointer, ItemPointer {

    override fun toString(): String {
        return "null"
    }

    override fun update(itemPointer: ItemPointer) {
        // Do nothing
    }

    override fun equals(other: Any?): Boolean {
        return other is NullPointer
    }
}