package io.github.shoaky.sourcedownloader.sdk

/**
 * 用于标识[Source]中的一个Item
 */
interface ItemPointer {

    companion object {

        fun of(id: String, value: Any): ItemPointer {
            return SimpleItemPointer(id, value)
        }
    }
}