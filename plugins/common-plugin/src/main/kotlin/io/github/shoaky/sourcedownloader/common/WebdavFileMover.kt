package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.webdav.*
import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.http.StatusCodes
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

/**
 * Webdav协议文件移动器，可以理解为上传到Webdav服务器
 */
open class WebdavFileMover(
    private val webdavClient: WebdavClient,
    private val uploadFileMode: Boolean = true,
    private val deleteSource: Boolean = true,
) : FileMover {

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        if (uploadFileMode) {
            return uploadFile(file)
        }

        val webdavPath = webdavClient.webdavPath
        val src = file.fileDownloadPath.toString()
        val dst = file.targetPath().toString()
        val moveFile = MoveFile("$webdavPath$src", "$webdavPath$dst")
        val resp = webdavClient.execute(moveFile)
        return resp.statusCode() == StatusCodes.CREATED
    }

    private fun uploadFile(file: FileContent): Boolean {
        val webdavPath = webdavClient.webdavPath
        val target = file.targetPath().toString()
        val uploadFile = UploadFile("$webdavPath$target", file.fileDownloadPath)
        val resp = webdavClient.execute(uploadFile)
        if (resp.statusCode() != StatusCodes.CREATED) {
            log.error("Failed to create file: {}, code: {} body:{}", file.targetPath(), resp.statusCode(), resp.body())
            return false
        }
        if (deleteSource) {
            file.fileDownloadPath.deleteIfExists()
        }
        return true
    }

    override fun exists(paths: List<Path>): List<Boolean> {
        return paths.map {
            webdavClient.execute(FindProp(it.toString())).statusCode() == StatusCodes.OK
        }
    }

    override fun createDirectories(path: Path) {
        val createDirectory = CreateDirectory(path.toString())
        val resp = webdavClient.execute(createDirectory)
        if (resp.statusCode() != StatusCodes.OK) {
            log.error("Failed to create directory: $path, code: ${resp.statusCode()} body:${resp.body()}")
        }
    }

    override fun replace(itemContent: ItemContent): Boolean {
        itemContent.fileContents.forEach {
            move(itemContent.sourceItem, it)
        }
        return true
    }

    override fun listFiles(path: Path): List<Path> {
        return super.listFiles(path)
    }

    override fun pathMetadata(path: Path): SourceFile {
        return super.pathMetadata(path)
    }

    companion object {

        private val log = LoggerFactory.getLogger(WebdavFileMover::class.java)
    }

}