package io.github.shoaky.sourcedownloader.sdk

import io.github.shoaky.sourcedownloader.sdk.component.Source

/**
 * 提供给[Source]数据读取到什么位置了
 * 该数据会被持久化，用于下一次传递给[Source]时，从上次读取的位置开始读取
 *
 */
interface SourcePointer {

    /**
     * 当Process每处理完一个Item都会调用一次该方法，用于更新[SourcePointer]的状态
     */
    fun update(itemPointer: ItemPointer)
}

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

interface ItemPointer {

    companion object {

        fun of(id: String, value: Any): ItemPointer {
            return SimpleItemPointer(id, value)
        }
    }
}

data class SimpleItemPointer<T>(
    val id: String,
    val value: T
) : ItemPointer