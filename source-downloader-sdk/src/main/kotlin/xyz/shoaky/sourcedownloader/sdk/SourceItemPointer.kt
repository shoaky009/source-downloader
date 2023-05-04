package xyz.shoaky.sourcedownloader.sdk


interface SourceItemPointer
object NullPointer : SourceItemPointer

data class OffsetPointer(
    val offset: Long
) : SourceItemPointer