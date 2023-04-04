package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface FileContent {

    val fileDownloadPath: Path
    val patternVariables: PatternVariables

    fun targetPath(): Path

    /**
     * 这个待定,主要为了减少一些存储空间
     */
    fun addExternalVariables(variables: PatternVariables) {}

    fun saveDirectoryPath(): Path {
        return targetPath().parent
    }

    fun itemFileRootDirectory(): Path?
}