package io.github.shoaky.sourcedownloader.application.spring.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * @suppress
 */
@Controller
class SpaController {

    @GetMapping("/{path:[^.]*}")
    fun forward(@PathVariable path: String): String {
        return "forward:/"
    }
}