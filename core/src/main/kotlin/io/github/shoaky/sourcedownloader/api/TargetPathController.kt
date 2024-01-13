package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/target-path")
class TargetPathController(
    private val processingStorage: ProcessingStorage
) {

    @DeleteMapping
    fun deleteTargetPaths(@RequestBody paths: List<String>) {
        processingStorage.deleteTargetPaths(paths, null)
    }

}