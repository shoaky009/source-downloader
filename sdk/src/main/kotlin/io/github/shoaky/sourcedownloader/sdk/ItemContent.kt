package io.github.shoaky.sourcedownloader.sdk

interface ItemContent {

    val sourceItem: SourceItem
    val fileContents: List<FileContent>
    val itemVariables: PatternVariables

    fun summaryContent(): String
}

interface PathPattern {

    val pattern: String

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
