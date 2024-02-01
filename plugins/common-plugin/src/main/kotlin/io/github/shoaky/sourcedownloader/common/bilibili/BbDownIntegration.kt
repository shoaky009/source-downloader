package io.github.shoaky.sourcedownloader.common.bilibili

import io.github.shoaky.sourcedownloader.common.anime.pathSegments
import io.github.shoaky.sourcedownloader.external.bbdown.AddTask
import io.github.shoaky.sourcedownloader.external.bbdown.BbDownClient
import io.github.shoaky.sourcedownloader.external.bbdown.DeleteTask
import io.github.shoaky.sourcedownloader.external.bbdown.GetTask
import io.github.shoaky.sourcedownloader.sdk.DownloadTask
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AsyncDownloader
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path
import kotlin.io.path.Path

class BbDownIntegration(
    private val downloadPath: Path,
    private val bbDownClient: BbDownClient,
    private val options: BbDownOptions = BbDownOptions(),
) : AsyncDownloader, ItemFileResolver {

    override fun isFinished(sourceItem: SourceItem): Boolean? {
        val aid = extractAid(sourceItem)
        val response = bbDownClient.execute(GetTask(aid))
        if (response.statusCode() != 200) {
            return null
        }

        val body = response.body()
        return body.contains("任务完成")
    }

    override fun submit(task: DownloadTask): Boolean {
        val bv = extractBv(task.sourceItem)
        val response = bbDownClient.execute(AddTask(bv, options))
        return response.statusCode() == 200
    }

    override fun defaultDownloadPath(): Path {
        return downloadPath
    }

    override fun cancel(sourceItem: SourceItem, files: List<SourceFile>) {
        val aid = extractAid(sourceItem)
        bbDownClient.execute(DeleteTask(aid))
    }

    private fun extractBv(sourceItem: SourceItem): String {
        val bvRegex = Regex("BV[0-9a-zA-Z]{10}")
        return sourceItem.downloadUri.pathSegments().firstNotNullOfOrNull { bvRegex.find(it)?.value }
            ?: throw IllegalArgumentException("BV not found in ${sourceItem.downloadUri}")
    }

    private fun extractAid(sourceItem: SourceItem): String {
        val aidRegex = Regex("[0-9]+")
        return sourceItem.link.pathSegments().firstNotNullOfOrNull { aidRegex.find(it)?.value }
            ?: throw IllegalArgumentException("Aid not found in ${sourceItem.downloadUri}")
    }

    /**
     * 暂时用标题+BV号固定死,有空再根据配置的模板来调整
     */
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val page = sourceItem.getAttr<Int>("page") ?: 1
        val bv = sourceItem.getAttr<String>("bv")
        val basicName = "${sourceItem.title}_$bv.mp4"
        if (page == 1) {
            return listOf(
                SourceFile(
                    Path(basicName)
                )
            )
        }

        val padLength = page.toString().length
        return IntRange(1, page).map {
            val seq = it.toString().padStart(padLength, '0')
            val name = "${basicName}_p$seq.mp4"
            SourceFile(
                Path(name)
            )
        }
    }

}

data class BbDownOptions(
    val cookie: String? = null,
    val filePattern: String? = "<videoTitle>_<bvid>",
    val workDir: String? = null,
    val selectPage: String? = null,
    val multiFilePattern: String? = "<videoTitle>_<bvid>_p<pageNumberWithZero>",
    val dfnPriority: String? = null,
    val downloadDanmaku: Boolean? = null,
)