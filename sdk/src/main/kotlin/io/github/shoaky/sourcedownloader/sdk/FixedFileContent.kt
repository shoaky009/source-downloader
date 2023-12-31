package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

/**
 * A [FileContent] implementation for testing.
 */
data class FixedFileContent(
    override val fileDownloadPath: Path,
    override val downloadPath: Path = Path.of(""),
    override val patternVariables: PatternVariables = PatternVariables.EMPTY,
    override val tags: Set<String> = emptySet(),
    val targetPath: Path = fileDownloadPath,
    val saveDirectoryPath: Path = fileDownloadPath,
    val itemSaveRootDirectory: Path = fileDownloadPath,
    val itemDownloadRootDirectory: Path = fileDownloadPath,
    override val attrs: Map<String, Any> = emptyMap(),
    override val fileUri: URI? = null,
    override val existTargetPath: Path? = null,
    val status: FileStatus = object : FileStatus {
        override fun status(): String = "NORMAL"
        override fun isSuccessful(): Boolean = true
    }
) : FileContent {

    override fun targetPath(): Path = targetPath

    override fun fileSaveRootDirectory(): Path = itemSaveRootDirectory

    override fun fileDownloadRootDirectory(): Path = itemDownloadRootDirectory

    override fun saveDirectoryPath(): Path = saveDirectoryPath

    override fun status(): FileStatus {
        return status
    }
}