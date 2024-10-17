package io.github.shoaky.sourcedownloader.application.spring.component

import io.github.shoaky.sourcedownloader.component.trigger.WebhookTrigger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser

class SpringWebFrameworkAdapter(
    private val requestMapping: RequestMappingHandlerMapping
) : WebhookTrigger.Adapter {

    private val registeredMappings = mutableMapOf<String, RequestMappingInfo>()

    override fun registerEndpoint(path: String, method: String, handler: () -> Unit) {
        val options = RequestMappingInfo.BuilderConfiguration()
        options.patternParser = PathPatternParser()
        val info = RequestMappingInfo.paths(path)
            .methods(RequestMethod.valueOf(method))
            .produces(MediaType.APPLICATION_JSON_VALUE)
            .options(options)
            .build()
        registeredMappings[path] = info
        requestMapping.registerMapping(info, handler, handler::class.java.methods.first { it.name == "invoke" })
    }

    override fun unregisterEndpoint(path: String, method: String) {
        registeredMappings[path]?.let {
            requestMapping.unregisterMapping(it)
        }
    }
}