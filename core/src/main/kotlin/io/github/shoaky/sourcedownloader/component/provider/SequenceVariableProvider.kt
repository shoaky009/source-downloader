package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 为每个文件提供一个序号
 */
object SequenceVariableProvider : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        val length = sourceFiles.size.toString().length
        return List(sourceFiles.size) { index ->
            val variables = MapPatternVariables(
                mapOf("sequence" to "${index + 1}".padStart(length, '0'))
            )
            variables
        }
    }

    override fun primaryVariableName(): String {
        return "sequence"
    }

}