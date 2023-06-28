package io.github.shoaky.sourcedownloader.sdk

import kotlin.io.path.name

interface SourceContent {

    val sourceFiles: List<FileContent>
    val sourceItem: SourceItem

    fun summaryContent(): String {
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
        val results: List<ExpressionResult>
    ) {

        fun success(): Boolean {
            return results.all { it.success }
        }

        fun failedExpression(): List<String> {
            return results.filter { !it.success }.map { it.expression }
        }
    }

    data class ExpressionResult(
        val expression: String,
        val success: Boolean = true
    )
}
