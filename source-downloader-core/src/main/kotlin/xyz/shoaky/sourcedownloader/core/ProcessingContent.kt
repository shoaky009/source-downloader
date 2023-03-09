package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.util.EnumValue
import java.time.LocalDateTime

data class ProcessingContent(
    val processorName: String,
    val sourceHash: String,
    val sourceContent: SourceContent,
    val renameTimes: Int = 0,
    val status: Status = Status.WAITING_TO_RENAME,
    val modifyTime: LocalDateTime? = null,
    val createTime: LocalDateTime = LocalDateTime.now()
) {
    constructor(processorName: String, sourceContent: SourceContent) : this(
        processorName = processorName,
        sourceHash = sourceContent.sourceItem.hashing(),
        sourceContent = sourceContent
    )

    var id: Long? = null

    enum class Status(val value: Int) : EnumValue<Int> {

        WAITING_TO_RENAME(0),
        TARGET_ALREADY_EXISTS(4),
        RENAMED(5);

        override fun getValue(): Int {
            return value
        }
    }

}