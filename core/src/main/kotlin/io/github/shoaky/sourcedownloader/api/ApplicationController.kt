package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/application")
class ApplicationController(
    private val application: SourceDownloaderApplication,
    private val buildProperties: BuildProperties
) {

    @GetMapping("/info")
    fun getInfo(): Any {
        return buildProperties
    }

    @GetMapping("/reload")
    fun reload() {
        application.reload()
    }

}