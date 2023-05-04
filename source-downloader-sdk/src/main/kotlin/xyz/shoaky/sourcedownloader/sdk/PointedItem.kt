package xyz.shoaky.sourcedownloader.sdk

class PointedItem<T : SourceItemPointer>(
    val sourceItem: SourceItem,
    val pointer: T
)