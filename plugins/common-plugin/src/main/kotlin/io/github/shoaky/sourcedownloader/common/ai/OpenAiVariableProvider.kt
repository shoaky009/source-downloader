package io.github.shoaky.sourcedownloader.common.ai

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.external.openai.ChatCompletion
import io.github.shoaky.sourcedownloader.external.openai.ChatMessage
import io.github.shoaky.sourcedownloader.external.openai.OpenAiClient
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import kotlin.io.path.name

/**
 * 通过openai来解析文件名中的变量，resolveVariables填写需要的变量名和描述
 */
class OpenAiVariableProvider(
    private val openAiBaseUri: URI,
    private val openAiClient: OpenAiClient,
    private val systemRole: ChatMessage,
    private val primary: String? = null,
    private val model: String = "gpt-3.5-turbo",
    private val includeFile: Boolean = false
) : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        val chatCompletion = ChatCompletion(
            listOf(systemRole, ChatMessage.ofUser(sourceItem.title)),
            model,
        )
        val response = openAiClient.execute(openAiBaseUri, chatCompletion).body()
        val first = response.choices.map { it.message }.first()
        val variables = Jackson.fromJson(first.content, jacksonTypeRef<Map<String, String>>())
        return MapPatternVariables(variables)
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>,
    ): List<PatternVariables> {
        if (!includeFile) {
            return sourceFiles.map { PatternVariables.EMPTY }
        }
        return sourceFiles.map { file ->
            val path = file.path
            val chatCompletion = ChatCompletion(
                listOf(systemRole, ChatMessage.ofUser("${sourceItem.title} ${path.name}"))
            )
            val response = openAiClient.execute(openAiBaseUri, chatCompletion).body()
            val first = response.choices.map { it.message }.first()
            val variables = Jackson.fromJson(first.content, jacksonTypeRef<Map<String, String>>())
            MapPatternVariables(variables)
        }
    }

    override fun primary(): String? {
        return primary
    }

    data class OpenAiConfig(
        val apiKeys: List<String>,
        val resolveVariables: List<String> = emptyList(),
        val apiHost: URI = URI("https://api.openai.com"),
        val systemRole: String = """
            你现在是一个文件解析器，从文件名中解析信息
            需要的信息有:${resolveVariables}
            如果不存在字段无需返回，不返回其他会干扰json解析的字符
        """.trimIndent(),
        val model: String = "gpt-3.5-turbo",
    )
}