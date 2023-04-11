package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.util.EnumValue
import java.time.LocalDateTime

data class ProcessingContent(
    var id: Long? = null,
    val processorName: String,
    val sourceHash: String,
    val sourceContent: PersistentSourceContent,
    val renameTimes: Int = 0,
    val status: Status = Status.WAITING_TO_RENAME,
    val modifyTime: LocalDateTime? = null,
    val createTime: LocalDateTime = LocalDateTime.now()
) {
    constructor(processorName: String, sourceContent: PersistentSourceContent) : this(
        processorName = processorName,
        sourceHash = sourceContent.sourceItem.hashing(),
        sourceContent = sourceContent
    )

    enum class Status(val value: Int) : EnumValue<Int> {

        WAITING_TO_RENAME(0),
        DOWNLOAD_FAILED(3),
        TARGET_ALREADY_EXISTS(4),
        RENAMED(5),
        DOWNLOADED(6),
        NO_FILES(7),
        ;

        override fun getValue(): Int {
            return value
        }
    }

}