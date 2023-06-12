package io.github.shoaky.sourcedownloader.sdk

data class PointedItem<T : SourceItemPointer>(
    val sourceItem: SourceItem,
    val pointer: T
)