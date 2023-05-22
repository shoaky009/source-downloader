package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.util.EnumValue
import java.time.LocalDateTime


data class ProcessingContent(
    var id: Long? = null,
    val processorName: String,
    val sourceHash: String,
    val sourceContent: PersistentSourceContent,
    val renameTimes: Int = 0,
    val status: Status = Status.WAITING_TO_RENAME,
    val failureReason: String? = null,
    val modifyTime: LocalDateTime? = null,
    val createTime: LocalDateTime = LocalDateTime.now()
) {
    constructor(processorName: String, sourceContent: PersistentSourceContent) : this(
        processorName = processorName,
        sourceHash = sourceContent.sourceItem.hashing(),
        sourceContent = sourceContent
    )

    enum class Status(val value: Int) : EnumValue<Int> {

        /**
         * 一切正常，下载完成后重命名
         */
        WAITING_TO_RENAME(0),

        /**
         * 下载失败，指从下载器获取[SourceItem]对应的信息失败，大概率是人工手动删除了
         */
        DOWNLOAD_FAILED(3),

        /**
         * 目标文件存在
         */
        TARGET_ALREADY_EXISTS(4),

        /**
         * 已重命名
         */
        RENAMED(5),

        /**
         * 已存在在下载目录中
         */
        DOWNLOADED(6),

        /**
         * [SourceItem]无文件
         */
        NO_FILES(7),

        /**
         * 处理失败
         */
        FAILURE(8)
        ;

        override fun getValue(): Int {
            return value
        }
    }

}