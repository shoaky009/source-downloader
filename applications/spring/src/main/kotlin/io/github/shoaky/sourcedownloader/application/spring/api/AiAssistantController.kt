package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.application.spring.ai.AiAssistantService
import io.github.shoaky.sourcedownloader.application.spring.ai.AiApplyRequest
import io.github.shoaky.sourcedownloader.application.spring.ai.AiApplyResponse
import io.github.shoaky.sourcedownloader.application.spring.ai.AiDraftRequest
import io.github.shoaky.sourcedownloader.application.spring.ai.AiDraftResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai")
private class AiAssistantController(
    private val aiAssistantService: AiAssistantService,
) {

    @PostMapping("/draft")
    fun draft(@RequestBody request: AiDraftRequest): AiDraftResponse {
        return aiAssistantService.draft(request)
    }

    @PostMapping("/apply")
    fun apply(@RequestBody request: AiApplyRequest): AiApplyResponse {
        return aiAssistantService.apply(request)
    }
}
