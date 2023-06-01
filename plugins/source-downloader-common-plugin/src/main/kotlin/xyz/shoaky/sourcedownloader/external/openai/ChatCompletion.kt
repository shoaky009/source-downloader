package xyz.shoaky.sourcedownloader.external.openai

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatCompletion(
    val messages: List<ChatMessage>,
    val model: String = "gpt-3.5-turbo",
    val temperature: Double = 1.0,
    val stream: Boolean = false,
    @JsonProperty("max_tokens")
    val maxTokens: Int? = null
) : BaseRequest<ChatResponse>() {

    override val path: String = "/v1/chat/completions"
    override val responseBodyType: TypeReference<ChatResponse> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatMessage(
    val role: String,
    val content: String,
    val name: String? = null
) {
    companion object {
        fun ofSystem(content: String): ChatMessage {
            return ChatMessage("system", content)
        }

        fun ofUser(content: String): ChatMessage {
            return ChatMessage("user", content)
        }
    }

}

data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val usage: Map<String, Any>,
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage,
    @JsonProperty("finish_reason")
    val finishReason: String,
    val index: Int
)