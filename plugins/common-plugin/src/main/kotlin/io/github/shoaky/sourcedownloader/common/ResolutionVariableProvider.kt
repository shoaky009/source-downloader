package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.nameWithoutExtension

class ResolutionVariableProvider(
    onlyHighResolution: Boolean = true
) : VariableProvider {

    private val regexes = if (onlyHighResolution) {
        resolutionMapping.filterValues { it.contains("HD").not() }
    } else {
        resolutionMapping
    }

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return PatternVariables.EMPTY
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>
    ): List<PatternVariables> {
        return sourceFiles.map { file ->
            val resolution =
                regexes.entries.firstOrNull { it.key.containsMatchIn(file.path.nameWithoutExtension) }?.value
            if (resolution == null) {
                return@map PatternVariables.EMPTY
            }
            MapPatternVariables(mapOf("resolution" to resolution))
        }
    }

    override fun primaryVariableName(): String? {
        return null
    }

    companion object {

        private val resolutionMapping = mapOf(
            "1920x1080".toRegex(RegexOption.IGNORE_CASE) to "FullHD",
            "1280x720".toRegex(RegexOption.IGNORE_CASE) to "HD",
            "2560x1440".toRegex(RegexOption.IGNORE_CASE) to "2K",
            "3840x2160".toRegex(RegexOption.IGNORE_CASE) to "4K",
            "7680x4320".toRegex(RegexOption.IGNORE_CASE) to "8K",
            "4K".toRegex() to "4K",
            "2K".toRegex() to "2K",
            "8K".toRegex() to "8K",
        )
    }
}