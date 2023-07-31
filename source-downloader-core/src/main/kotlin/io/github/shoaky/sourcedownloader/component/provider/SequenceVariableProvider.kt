package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

object SequenceVariableProvider : VariableProvider {

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return object : SourceItemGroup {
            override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
                return List(paths.size) { index ->
                    UniversalFileVariable(
                        MapPatternVariables(
                            mapOf(
                                "sequence" to "${index + 1}"
                            )
                        )
                    )
                }
            }
        }
    }

    override fun support(item: SourceItem): Boolean {
        return true
    }
}