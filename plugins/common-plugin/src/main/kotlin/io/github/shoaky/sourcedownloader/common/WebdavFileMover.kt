package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.webdav.*
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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

    override fun move(itemContent: ItemContent): Boolean {
        if (uploadFileMode) {
            return uploadFile(itemContent)
        }

        val webdavPath = webdavClient.webdavPath
        return itemContent.fileContents.map {
            val src = it.fileDownloadPath.toString()
            val dst = it.targetPath().toString()
            val moveFile = MoveFile("$webdavPath$src", "$webdavPath$dst")
            webdavClient.execute(moveFile)
        }.all { it.statusCode() == HttpStatus.CREATED.value() }
    }

    private fun uploadFile(itemContent: ItemContent): Boolean {
        val webdavPath = webdavClient.webdavPath
        return itemContent.fileContents.map {
            val target = it.targetPath().toString()
            val uploadFile = UploadFile("$webdavPath$target", it.fileDownloadPath)
            val resp = webdavClient.execute(uploadFile)
            if (resp.statusCode() != HttpStatus.CREATED.value()) {
                log.error("Failed to create file: {}, code: {} body:{}", it.targetPath(), resp.statusCode(), resp.body())
                return@map false
            }
            if (deleteSource) {
                it.fileDownloadPath.deleteIfExists()
            }
            true
        }.all { it }
    }

    override fun exists(paths: List<Path>): List<Boolean> {
        return paths.map {
            webdavClient.execute(FindProp(it.toString())).statusCode() == HttpStatus.OK.value()
        }
    }

    override fun createDirectories(path: Path) {
        val createDirectory = CreateDirectory(path.toString())
        val resp = webdavClient.execute(createDirectory)
        if (resp.statusCode() != HttpStatus.OK.value()) {
            log.error("Failed to create directory: $path, code: ${resp.statusCode()} body:${resp.body()}")
        }
    }

    override fun replace(itemContent: ItemContent): Boolean {
        return move(itemContent)
    }

    override fun listPath(path: Path): List<Path> {
        // TODO implement
        return super.listPath(path)
    }

    override fun pathMetadata(path: Path): SourceFile {
        // TODO implement
        return super.pathMetadata(path)
    }

    companion object {

        private val log = LoggerFactory.getLogger(WebdavFileMover::class.java)
    }

}