package io.github.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface FileVariable {

    /**
     * @return 模版变量
     */
    fun patternVariables(): PatternVariables

    companion object {
        @JvmStatic
        val EMPTY: FileVariable = EmptyFileVariable
    }
}

private object EmptyFileVariable : FileVariable {
    override fun patternVariables(): PatternVariables = PatternVariables.EMPTY

}

data class SourceFile(
    val path: Path,
    val attributes: Map<String, Any> = emptyMap()
)