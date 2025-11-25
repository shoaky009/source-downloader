package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.external.chii.ChiiClient
import io.github.shoaky.sourcedownloader.external.chii.SubjectQueryChiiRequest
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider

class ChiiVariableProvider(
    private val chiiClient: ChiiClient = ChiiClient(),
) : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return request(sourceItem.title)
    }

    private fun request(text: String): PatternVariables {
        val body = chiiClient.execute(SubjectQueryChiiRequest(text)).body()
        val subject = body.data.querySubjectSearch.result.firstOrNull() ?: return PatternVariables.EMPTY
        return MapPatternVariables(
            mapOf(
                "bgmtvId" to subject.id,
                "subjectName" to subject.name,
                "subjectNameCn" to subject.nameCn,
            )
        )
    }

    override fun extractFrom(sourceItem: SourceItem, text: String): PatternVariables {
        return request(text)
    }

    override fun primaryVariableName(): String {
        return "subjectName"
    }
}