package io.github.shoaky.sourcedownloader.component.trigger

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser
import kotlin.concurrent.thread

/**
 * Webhook触发器
 */
class WebhookTrigger(
    private val path: String,
    private val method: String = "GET",
    private val requestMapping: RequestMappingHandlerMapping
) : HoldingTaskTrigger() {
    override fun start() {
        val info = requestMappingInfo()
        requestMapping.registerMapping(info, this,
            WebhookTrigger::class.java.methods.first { it.name == "endpoint" })
    }

    @Suppress("UNUSED")
    fun endpoint(): ResponseEntity<Any> {
        for (task in tasks) {
            thread {
                task.run()
            }
        }
        return ResponseEntity.noContent().build()
    }

    private fun requestMappingInfo(): RequestMappingInfo {
        val options = RequestMappingInfo.BuilderConfiguration()
        options.patternParser = PathPatternParser()
        return RequestMappingInfo.paths("/webhook/$path")
            .methods(RequestMethod.valueOf(method))
            .produces(MediaType.APPLICATION_JSON_VALUE)
            .options(options)
            .build()
    }

    override fun stop() {
        requestMapping.unregisterMapping(requestMappingInfo())
    }

}

