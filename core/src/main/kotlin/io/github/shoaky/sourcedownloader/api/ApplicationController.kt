package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Application相关接口
 */
@RestController
@RequestMapping("/api/application")
class ApplicationController(
    private val application: SourceDownloaderApplication,
) {

    /**
     * 重载应用
     */
    @GetMapping("/reload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reload() {
        application.reload()
    }

}