package io.github.shoaky.sourcedownloader.common.ai

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.external.openai.ChatCompletion
import io.github.shoaky.sourcedownloader.external.openai.ChatMessage
import io.github.shoaky.sourcedownloader.external.openai.OpenAiClient
import io.github.shoaky.sourcedownloader.sdk.*
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
    private val systemRole: ChatMessage
) : VariableProvider {

    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return AiSourceGroup(openAiClient, systemRole, openAiBaseUri, sourceItem)
    }

    override fun support(item: SourceItem): Boolean = true

    data class OpenAiConfig(
        val apiKeys: List<String>,
        val resolveVariables: List<String> = emptyList(),
        val apiHost: URI = URI("https://api.openai.com"),
        val systemRole: String = """
            你现在是一个文件解析器，从文件名中解析信息
            需要的信息有:${resolveVariables}
            如果不存在字段无需返回，不返回其他会干扰json解析的字符
        """.trimIndent()
    )
}


private class AiSourceGroup(
    val openAiClient: OpenAiClient,
    val systemRole: ChatMessage,
    val uri: URI,
    val sourceItem: SourceItem,
) : SourceItemGroup {
    override fun filePatternVariables(paths: List<SourceFile>): List<FileVariable> {
        return paths.map { file ->
            val path = file.path
            val chatCompletion = ChatCompletion(
                listOf(systemRole, ChatMessage.ofUser("${sourceItem.title} ${path.name}"))
            )
            val response = openAiClient.execute(uri, chatCompletion).body()
            val first = response.choices.map { it.message }.first()
            val variables = Jackson.fromJson(first.content, jacksonTypeRef<Map<String, String>>())
            UniversalFileVariable(MapPatternVariables(variables))
        }
    }
}