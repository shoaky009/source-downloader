package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 只是没ui临时用着
@RestController
@RequestMapping("/api/application")
class ApplicationController(
    private val application: SourceDownloaderApplication
) {

    @GetMapping("/reload")
    fun reload() {
        application.reload()
    }

}