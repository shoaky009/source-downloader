package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication

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