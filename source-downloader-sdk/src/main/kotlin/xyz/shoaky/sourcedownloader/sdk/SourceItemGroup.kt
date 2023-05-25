package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface SourceItemGroup {

    /**
     * @return 位于DownloadPath下的相对路径
     */
    fun sourceFiles(paths: List<Path>): List<SourceFile>

    /**
     * @return 和SourceFile共享的变量
     */
    fun sharedPatternVariables() = PatternVariables.EMPTY

    companion object {
        val EMPTY = object : SourceItemGroup {
            override fun sourceFiles(paths: List<Path>): List<SourceFile> = paths.map { SourceFile.EMPTY }
        }
    }
}