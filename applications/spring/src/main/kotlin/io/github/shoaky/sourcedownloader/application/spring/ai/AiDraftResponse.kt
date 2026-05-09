package io.github.shoaky.sourcedownloader.application.spring.ai

import io.github.shoaky.sourcedownloader.service.AssistantQuestion
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraft
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraftResponse

data class AiDraftRequest(
    val input: String = "",
    val sessionId: String? = null,
    val answers: Map<String, Any> = emptyMap(),
    val draft: ConfigAssistantDraftResponse? = null,
)

data class AiDraftResponse(
    val sessionId: String,
    val content: String,
    val draft: ConfigAssistantDraftResponse?,
    val askedQuestion: AssistantQuestion?,
    val nextAction: AiNextAction,
)

data class AiApplyRequest(
    val sessionId: String? = null,
    val draft: ConfigAssistantDraft,
)

data class AiApplyResponse(
    val sessionId: String,
    val processorName: String,
    val content: String,
)

enum class AiNextAction {
    ASK_QUESTION,
    APPLY,
}
