package xyz.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.annotation.JsonValue
import java.nio.file.Path
import java.util.regex.Pattern
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

data class PathPattern(
    @get:JsonValue
    val pattern: String
) {
    fun parse(provider: PatternVariables): ParseResult {
        val matcher = VARIABLE_PATTERN.matcher(pattern)
        val result = StringBuilder()
        val variables = provider.variables()
        val varRes = mutableListOf<VariableResult>()
        while (matcher.find()) {
            val variableName = matcher.group(1)
            val variableValue = variables[variableName]
            varRes.add(VariableResult(variableName, variableValue != null))
            if (variableValue != null) {
                matcher.appendReplacement(result, variableValue)
            }
        }
        matcher.appendTail(result)
        return ParseResult(result.toString(), varRes)
    }

    fun depth(): Int {
        return pattern.split("/").size
    }

    companion object {
        val VARIABLE_PATTERN: Pattern = Pattern.compile("\\{(.+?)}")
        val ORIGIN = PathPattern("")
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
