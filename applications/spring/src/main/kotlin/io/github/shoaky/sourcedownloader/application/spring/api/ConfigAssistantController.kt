package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.service.ConfigAssistantApplyRequest
import io.github.shoaky.sourcedownloader.service.ConfigAssistantApplyResponse
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraftRequest
import io.github.shoaky.sourcedownloader.service.ConfigAssistantDraftResponse
import io.github.shoaky.sourcedownloader.service.ConfigAssistantService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/config-assistant")
private class ConfigAssistantController(
    private val configAssistantService: ConfigAssistantService,
) {

    @PostMapping("/draft")
    fun draft(@RequestBody request: ConfigAssistantDraftRequest): ConfigAssistantDraftResponse {
        return configAssistantService.draft(request)
    }

    @PostMapping("/apply")
    fun apply(@RequestBody request: ConfigAssistantApplyRequest): ConfigAssistantApplyResponse {
        return configAssistantService.apply(request)
    }
}
