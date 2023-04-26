package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path
import java.util.function.Function

class FunctionalItemGroup(
    private val sharedVariables: PatternVariables = PatternVariables.EMPTY,
    private val function: Function<Path, SourceFile>,
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return paths.map {
            function.apply(it)
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return sharedVariables
    }
}