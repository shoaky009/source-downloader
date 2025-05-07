package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemContentFilter
import io.github.shoaky.sourcedownloader.util.EnumValue
import java.time.LocalDateTime

data class ProcessingContent(
    var id: Long? = null,
    val processorName: String,
    val itemHash: String,
    val itemIdentity: String?,
    val itemContent: CoreItemContent,
    val renameTimes: Int = 0,
    val status: Status = Status.WAITING_TO_RENAME,
    val failureReason: String? = null,
    val modifyTime: LocalDateTime? = null,
    val createTime: LocalDateTime = LocalDateTime.now()
) {

    constructor(processorName: String, itemContent: CoreItemContent) : this(
        processorName = processorName,
        itemHash = itemContent.sourceItem.hashing(),
        itemIdentity = itemContent.sourceItem.identity,
        itemContent = itemContent
    )

    /**
     * SourceItem处理状态
     */
    enum class Status(val value: Int) : EnumValue<Int> {

        /**
         * 下载完成后重命名，可能包含替换的文件
         */
        WAITING_TO_RENAME(0),

        /**
         * 被[ItemContentFilter]过滤
         */
        FILTERED(2),

        /**
         * 下载失败，指从下载器获取[SourceItem]对应的信息失败，大概率是人工手动删除了
         */
        DOWNLOAD_FAILED(3),

        /**
         * 全部目标文件存在
         */
        TARGET_ALREADY_EXISTS(4),

        /**
         * 已重命名
         */
        RENAMED(5),

        /**
         * [SourceItem]无文件
         */
        NO_FILES(7),

        /**
         * 处理失败
         */
        FAILURE(8),

        /**
         * 取消
         */
        CANCELLED(9)
        ;

        override fun getValue(): Int {
            return value
        }
    }

}