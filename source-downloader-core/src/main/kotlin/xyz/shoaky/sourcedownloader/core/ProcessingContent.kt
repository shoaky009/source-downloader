package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.DownloadTask
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import java.time.LocalDateTime

data class ProcessingContent(
    val processorName: String,
    val sourceHash: String,
    val sourceContent: SourceContent,
    val downloadTask: DownloadTask,
    val renameTimes: Int = 0,
    val modifyTime: LocalDateTime? = null,
    val createTime: LocalDateTime = LocalDateTime.now()
) {
    var id: Long? = null
}