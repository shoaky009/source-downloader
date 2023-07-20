package io.github.shoaky.sourcedownloader.sdk

data class PointedItem<out IP : ItemPointer>(
    val sourceItem: SourceItem,
    val pointer: IP
)