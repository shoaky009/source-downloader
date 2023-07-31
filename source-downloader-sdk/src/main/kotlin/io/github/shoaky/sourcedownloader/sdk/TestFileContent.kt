package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
import java.nio.file.Path

/**
 * A [FileContent] implementation for testing.
 */
data class TestFileContent(
    override val fileDownloadPath: Path,
    override val downloadPath: Path = Path.of(""),
    override val patternVariables: PatternVariables = PatternVariables.EMPTY,
    override val tags: Set<String> = emptySet(),
    val targetPath: Path = fileDownloadPath,
    val itemSaveRootDirectory: Path = fileDownloadPath,
    val itemDownloadRootDirectory: Path = fileDownloadPath,
    override val attrs: Map<String, Any> = emptyMap(),
    override val fileUri: URI? = null,
) : FileContent {

    override fun targetPath(): Path = targetPath

    override fun fileSaveRootDirectory(): Path = itemSaveRootDirectory

    override fun fileDownloadRootDirectory(): Path = itemDownloadRootDirectory
}