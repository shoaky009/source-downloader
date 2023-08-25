package io.github.shoaky.sourcedownloader.sdk

interface SourceItemGroup {

    /**
     * @return 位于DownloadPath下的相对路径
     */
    fun filePatternVariables(paths: List<SourceFile>): List<FileVariable>

    fun filePatternVariables(vararg sourceFiles: SourceFile): List<FileVariable> {
        return filePatternVariables(sourceFiles.toList())
    }

    /**
     * @return shared pattern variables, which will be used in all filePatternVariables
     */
    fun sharedPatternVariables() = PatternVariables.EMPTY

    companion object {
        val EMPTY = object : SourceItemGroup {
            override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> =
                paths.map { FileVariable.EMPTY }
        }
    }
}