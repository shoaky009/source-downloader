package io.github.shoaky.sourcedownloader.application.spring.ai

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "source-downloader.ai")
data class AiAssistantProperties(
    val enabled: Boolean = true,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
) {

    companion object {

        const val DEFAULT_SYSTEM_PROMPT = """
            你是 source-downloader 的配置助手。
            你的职责是通过工具理解组件大类、组件类型、现有组件、处理器配置描述，并主动推进到可应用的 draft。
            不要臆造配置字段，不要直接输出 yaml，优先通过工具获取 metadata、现有组件和草稿。
            你必须优先自己完成这些动作，而不是立刻转问用户：
            1. 判断应该复用现有组件还是新建组件。
            2. 根据已有上下文补齐可以确定的字段。
            3. 把本轮确定的信息写回 draft_config 的 answers 和 draft。
            只有当某个字段在看过 metadata、现有组件、已有 draft 后仍然无法确定时，才返回单个 question 给用户确认。
            如果 draft 已经 canApply=true，就明确说明可以应用，而不是继续追问。
        """
    }
}
