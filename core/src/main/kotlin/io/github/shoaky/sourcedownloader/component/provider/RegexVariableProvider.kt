package io.github.shoaky.sourcedownloader.component.provider

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

class RegexVariableProvider(
    private val regexes: List<RegexVariable>
) : VariableProvider {

    override fun itemSharedVariables(sourceItem: SourceItem): PatternVariables {
        val variables = regexes.mapNotNull { regexVariable ->
            val find = regexVariable.regex.find(resolveField(sourceItem, regexVariable))
            find?.let { regexVariable.name to it.value }
        }.toMap()

        return MapPatternVariables(variables)
    }

    private fun resolveField(sourceItem: SourceItem, regexVariable: RegexVariable): CharSequence {
        return when (regexVariable.field) {
            "title" -> sourceItem.title
            "link" -> sourceItem.link.toString()
            "downloadUri" -> sourceItem.downloadUri.toString()
            "contentType" -> sourceItem.contentType
            "datetime" -> sourceItem.datetime.toString()
            else -> throw IllegalArgumentException("Unknown field ${regexVariable.field}")
        }
    }

    override fun extractFrom(text: String): String {
        return regexes.fold(text) { acc, regexVariable ->
            regexVariable.regex.find(acc)?.value ?: acc
        }
    }

    override fun support(sourceItem: SourceItem): Boolean {
        return true
    }
}

data class RegexVariable(
    val name: String,
    val regex: Regex,
    val field: String = "title"
)