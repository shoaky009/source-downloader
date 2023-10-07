package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

/**
 * 为每个文件提供一个序号
 */
object SequenceVariableProvider : VariableProvider {

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return object : SourceItemGroup {
            override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
                val length = paths.size.toString().length
                return List(paths.size) { index ->
                    val variables = MapPatternVariables(
                        mapOf("sequence" to "${index + 1}".padStart(length, '0'))
                    )
                    UniversalFileVariable(variables)
                }
            }
        }
    }

    override fun support(item: SourceItem): Boolean {
        return true
    }
}