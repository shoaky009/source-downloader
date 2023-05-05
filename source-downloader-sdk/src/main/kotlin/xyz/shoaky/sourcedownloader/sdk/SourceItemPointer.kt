package xyz.shoaky.sourcedownloader.sdk

import xyz.shoaky.sourcedownloader.sdk.component.Source

/**
 * 提供给[Source]数据读取到什么位置了
 * 该数据会被持久化，用于下一次传递给[Source]时，从上次读取的位置开始读取
 *
 */
interface SourceItemPointer

object NullPointer : SourceItemPointer

data class OffsetPointer(
    val offset: Long
) : SourceItemPointer