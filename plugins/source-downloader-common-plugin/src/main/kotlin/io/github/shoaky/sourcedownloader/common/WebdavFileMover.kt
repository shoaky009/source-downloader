package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.external.webdav.*
import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

open class WebdavFileMover(
    private val webdavClient: WebdavClient,
    private val uploadFileMode: Boolean = true,
    private val deleteSource: Boolean = true,
) : FileMover {

    override fun move(sourceContent: SourceContent): Boolean {
        if (uploadFileMode) {
            return uploadFile(sourceContent)
        }

        val webdavPath = webdavClient.webdavPath
        return sourceContent.sourceFiles.map {
            val src = it.fileDownloadPath.toString()
            val dst = it.targetPath().toString()
            val moveFile = MoveFile("$webdavPath$src", "$webdavPath$dst")
            webdavClient.execute(moveFile)
        }.all { it.statusCode() == HttpStatus.CREATED.value() }
    }

    private fun uploadFile(sourceContent: SourceContent): Boolean {
        val webdavPath = webdavClient.webdavPath
        return sourceContent.sourceFiles.map {
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

    override fun exists(paths: List<Path>): Boolean {
        return paths.asSequence().map {
            webdavClient.execute(FindProp(it.toString()))
        }.any { it.statusCode() != HttpStatus.NOT_FOUND.value() }
    }

    override fun createDirectories(path: Path) {
        val createDirectory = CreateDirectory(path.toString())
        val resp = webdavClient.execute(createDirectory)
        if (resp.statusCode() != HttpStatus.OK.value()) {
            log.error("Failed to create directory: $path, code: ${resp.statusCode()} body:${resp.body()}")
        }
    }

    override fun replace(sourceContent: SourceContent): Boolean {
        return move(sourceContent)
    }

    companion object {

        private val log = LoggerFactory.getLogger(WebdavFileMover::class.java)
    }

}