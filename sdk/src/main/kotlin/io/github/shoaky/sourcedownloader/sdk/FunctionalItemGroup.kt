package io.github.shoaky.sourcedownloader.sdk

import io.github.shoaky.sourcedownloader.sdk.util.http.log
import java.util.function.Function

class FunctionalItemGroup(
    private val sharedVariables: PatternVariables = PatternVariables.EMPTY,
    private val function: Function<SourceFile, FileVariable>,
) : SourceItemGroup {

    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map {
            try {
                function.apply(it)
            } catch (e: Exception) {
                log.error("Error when handle file {}", it)
                throw e
            }
        }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return sharedVariables
    }
}