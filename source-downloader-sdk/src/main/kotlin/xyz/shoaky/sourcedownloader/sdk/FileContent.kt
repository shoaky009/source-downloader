package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface FileContent {

    val fileDownloadPath: Path
    val patternVariables: PatternVariables

    fun targetPath(): Path

    fun saveDirectoryPath(): Path {
        return targetPath().parent
    }

    fun itemFileRootDirectory(): Path?
}