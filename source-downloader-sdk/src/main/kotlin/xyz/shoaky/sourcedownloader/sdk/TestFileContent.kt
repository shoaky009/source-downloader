package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

/**
 * A [FileContent] implementation for testing.
 */
data class TestFileContent(
    override val fileDownloadPath: Path,
    override val downloadPath: Path = fileDownloadPath,
    override val patternVariables: PatternVariables = PatternVariables.EMPTY,
    override val tags: Set<String> = emptySet(),
    val targetPath: Path = fileDownloadPath,
    val itemSaveRootDirectory: Path = fileDownloadPath,
    val itemDownloadRootDirectory: Path = fileDownloadPath,
    override val attributes: Map<String, Any> = emptyMap()
) : FileContent {

    override fun targetPath(): Path = targetPath

    override fun itemSaveRootDirectory(): Path = itemSaveRootDirectory

    override fun itemDownloadRootDirectory(): Path = itemDownloadRootDirectory
}