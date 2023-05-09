package xyz.shoaky.sourcedownloader.ai

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.name

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
            如果不存在字段无需返回，不要有其他会干扰json解析的字符
        """.trimIndent()
    )
}


private class AiSourceGroup(
    val openAiClient: OpenAiClient,
    val systemRole: ChatMessage,
    val uri: URI,
    val sourceItem: SourceItem,
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return paths.map { path ->
            val chatCompletion = ChatCompletion(
                listOf(systemRole, ChatMessage.ofUser("${sourceItem.title} ${path.name}"))
            )
            val response = openAiClient.execute(uri, chatCompletion).body()
            val first = response.choices.map { it.message }.first()
            val variables = Jackson.fromJson(first.content, jacksonTypeRef<Map<String, String>>())
            UniversalSourceFile(MapPatternVariables(variables))
        }
    }
}