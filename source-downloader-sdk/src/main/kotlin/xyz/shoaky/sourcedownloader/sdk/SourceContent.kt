package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path
import kotlin.io.path.name

interface SourceContent {
    val sourceFiles: List<FileContent>
    val sourceItem: SourceItem

    fun allTargetPaths(): List<Path> {
        return sourceFiles.map { it.targetPath() }
    }

    fun summarySubject(): String {
        if (sourceFiles.size == 1) {
            return sourceFiles.first().targetPath().name
        }
        return "${sourceItem.title}内的${sourceFiles.size}个文件"
    }
}

interface PathPattern {
    val pattern: String
    fun parse(provider: PatternVariables): ParseResult

    fun depth(): Int {
        return pattern.split("/").size
    }

    data class ParseResult(
        val path: String,
        val results: List<VariableResult>
    ) {
        fun success(): Boolean {
            return results.all { it.success }
        }

        fun failedVariables(): List<String> {
            return results.filter { !it.success }.map { it.variable }
        }
    }

    data class VariableResult(
        val variable: String,
        val success: Boolean = true
    )
}
