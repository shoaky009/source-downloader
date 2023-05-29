package xyz.shoaky.sourcedownloader.telegram

import xyz.shoaky.sourcedownloader.sdk.FileVariable
import xyz.shoaky.sourcedownloader.sdk.PatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sdk.SourceItemGroup
import java.nio.file.Path

internal data class TelegramItemGroup(
    private val vars: TelegramVariable
) : SourceItemGroup {
    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map { FileVariable.EMPTY }
    }

    override fun sharedPatternVariables(): PatternVariables {
        return vars
    }
}