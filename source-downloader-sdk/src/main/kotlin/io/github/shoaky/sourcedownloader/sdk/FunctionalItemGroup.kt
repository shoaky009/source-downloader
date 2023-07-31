package io.github.shoaky.sourcedownloader.sdk

import java.util.function.Function

class FunctionalItemGroup(
    private val sharedVariables: PatternVariables = PatternVariables.EMPTY,
    private val function: Function<SourceFile, FileVariable>,
) : SourceItemGroup {
    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map {
            function.apply(it)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return sharedVariables
    }
}