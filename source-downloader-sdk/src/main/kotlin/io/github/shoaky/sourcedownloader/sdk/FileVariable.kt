package io.github.shoaky.sourcedownloader.sdk

import java.net.URI
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
    /**
     * The path of the file relative to the root of the source.
     */
    val path: Path,
    /**
     * The attributes of the file.
     */
    val attributes: Map<String, Any> = emptyMap(),
    /**
     * The URI of the file.
     */
    val fileUri: URI? = null,
)