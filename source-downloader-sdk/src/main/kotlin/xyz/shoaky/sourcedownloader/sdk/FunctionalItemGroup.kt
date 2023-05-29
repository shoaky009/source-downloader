package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path
import java.util.function.Function

class FunctionalItemGroup(
    private val sharedVariables: PatternVariables = PatternVariables.EMPTY,
    private val function: Function<Path, FileVariable>,
) : SourceItemGroup {
    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map {
            function.apply(it.path)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return sharedVariables
    }
}