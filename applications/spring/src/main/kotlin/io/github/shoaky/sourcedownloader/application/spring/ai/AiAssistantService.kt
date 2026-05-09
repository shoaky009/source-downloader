package io.github.shoaky.sourcedownloader.application.spring.ai

import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.service.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import java.util.*

class AiAssistantService(
    private val chatClientBuilder: ChatClient.Builder,
    private val props: AiAssistantProperties,
    private val configAssistantService: ConfigAssistantService,
    private val metadataMcpTools: MetadataMcpTools,
) {

    fun draft(request: AiDraftRequest): AiDraftResponse {
        val sessionId = request.sessionId ?: UUID.randomUUID().toString()
        val fallbackDraft = buildDraft(request)
        val askedQuestion = fallbackDraft.question
        val nextAction = if (fallbackDraft.canApply) AiNextAction.APPLY else AiNextAction.ASK_QUESTION
        if (!props.enabled) {
            return AiDraftResponse(
                sessionId = sessionId,
                content = fallbackMessage(fallbackDraft),
                draft = fallbackDraft,
                askedQuestion = askedQuestion,
                nextAction = nextAction,
            )
        }

        val userInput = request.input.trim()
        val content = try {
            chatClientBuilder.build()
                .prompt()
                .system(props.systemPrompt)
                .user(buildDraftUserPrompt(userInput, fallbackDraft, askedQuestion, nextAction))
                .options(
                    OpenAiChatOptions.builder()
                        .extraBody(
                            mapOf(
                                "reasoning_content" to "high"
                            )
                        )
                )
                .tools(metadataMcpTools)
                .call()
                .content()
        } catch (ex: RuntimeException) {
            if (!isReasoningToolCompatibilityError(ex)) {
                throw ex
            }
            chatClientBuilder.build()
                .prompt()
                .system(
                    """
                    ${props.systemPrompt}
                    
                    当前模型与工具调用的 reasoning 模式不兼容，不能继续调用工具。
                    你必须严格基于给定的后端草稿结果回答，不能补充不存在的字段。
                    请用简洁中文总结当前配置状态，并明确告诉用户下一步需要回答的问题。
                    """.trimIndent()
                )
                .user(
                    """
                    用户原始输入:
                    $userInput

                    后端草稿结果(JSON):
                    ${Jackson.toJsonString(fallbackDraft)}
                    """.trimIndent()
                )
                .call()
                .content()
        }

        return AiDraftResponse(
            sessionId = sessionId,
            content = content ?: fallbackMessage(fallbackDraft),
            draft = fallbackDraft,
            askedQuestion = askedQuestion,
            nextAction = nextAction,
        )
    }

    fun apply(request: AiApplyRequest): AiApplyResponse {
        val sessionId = request.sessionId ?: UUID.randomUUID().toString()
        val result = configAssistantService.apply(ConfigAssistantApplyRequest(request.draft))
        return AiApplyResponse(
            sessionId = sessionId,
            processorName = result.processorName,
            content = "处理器 ${result.processorName} 已创建。",
        )
    }

    private fun isReasoningToolCompatibilityError(ex: RuntimeException): Boolean {
        return generateSequence<Throwable>(ex) { it.cause }
            .mapNotNull { it.message }
            .any {
                it.contains("reasoning_content", ignoreCase = true) &&
                    it.contains("must be passed back", ignoreCase = true)
            }
    }

    private fun buildDraft(request: AiDraftRequest): ConfigAssistantDraftResponse {
        val previousDraft = request.draft?.draft
        val mergedAnswers = linkedMapOf<String, Any>()
        previousDraft?.answers?.let { mergedAnswers.putAll(it) }
        mergedAnswers.putAll(request.answers)
        val input = request.input.ifBlank { previousDraft?.normalizedInput.orEmpty() }
        return configAssistantService.draft(
            ConfigAssistantDraftRequest(
                input = input,
                answers = mergedAnswers,
                draft = previousDraft,
            )
        )
    }

    private fun buildDraftUserPrompt(
        input: String,
        draft: ConfigAssistantDraftResponse,
        askedQuestion: AssistantQuestion?,
        nextAction: AiNextAction,
    ): String {
        return buildString {
            appendLine("用户原始输入:")
            appendLine(input.ifBlank { "<empty>" })
            appendLine()
            appendLine("后端草稿结果(JSON):")
            appendLine(Jackson.toJsonString(draft))
            appendLine()
            appendLine("当前下一步动作: $nextAction")
            if (askedQuestion != null) {
                appendLine("当前最优先问题(JSON):")
                appendLine(Jackson.toJsonString(askedQuestion))
            }
            appendLine()
            appendLine("请基于这个草稿结果回答。")
            appendLine("如果 nextAction=ASK_QUESTION，不要立刻把问题转给用户。")
            appendLine("先判断是否可以通过工具查看现有组件、组件类型详情、上一轮 draft 和已有 answers 来补齐字段。")
            appendLine("只有在这些上下文都不足以继续推进时，才简洁说明当前理解并提出 askedQuestion。")
            appendLine("如果 nextAction=APPLY，就明确说明草稿已经可以应用。")
            appendLine("不要编造草稿里不存在的字段。")
        }
    }

    private fun fallbackMessage(draft: ConfigAssistantDraftResponse): String {
        return buildString {
            append("已生成配置草稿")
            val question = draft.question
            when {
                question != null -> {
                    append("，下一步请回答: ")
                    append(question.label)
                }

                draft.missingFields.isNotEmpty() -> {
                    append("，仍缺少: ")
                    append(draft.missingFields.joinToString { it.label })
                }

                else -> append("，现在可以应用")
            }
        }
    }
}
