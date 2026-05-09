package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.service.ComponentCreateBody

data class ConfigAssistantDraftRequest(
    val input: String,
    val answers: Map<String, Any> = emptyMap(),
    val draft: ConfigAssistantDraft? = null,
)

data class ConfigAssistantApplyRequest(
    val draft: ConfigAssistantDraft,
)

data class ConfigAssistantDraftResponse(
    val draft: ConfigAssistantDraft,
    val canApply: Boolean,
    val missingFields: List<MissingField>,
    val question: AssistantQuestion?,
)

data class ConfigAssistantDraft(
    val intentType: String,
    val summary: String,
    val normalizedInput: String,
    val answers: Map<String, Any>,
    val selectedComponentTypes: Map<String, String>,
    val componentDrafts: List<ComponentCreateBody>,
    val processorDraft: ProcessorConfig?,
)

data class MissingField(
    val key: String,
    val label: String,
    val description: String,
)

data class AssistantQuestion(
    val key: String,
    val label: String,
    val description: String? = null,
    val inputType: AssistantQuestionInputType = AssistantQuestionInputType.TEXT,
    val required: Boolean = true,
    val multiple: Boolean = false,
    val customAllowed: Boolean = false,
    val options: List<AssistantQuestionOption> = emptyList(),
    val defaultValue: Any? = null,
    val placeholder: String? = null,
)

enum class AssistantQuestionInputType {
    TEXT,
    SINGLE_SELECT,
    MULTI_SELECT,
    BOOLEAN,
}

data class AssistantQuestionOption(
    val label: String,
    val value: String,
    val description: String? = null,
    val recommended: Boolean = false,
    val disabled: Boolean = false,
)

data class ConfigAssistantApplyResponse(
    val processorName: String,
)
