package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * TargetPath相关的API
 */
@RestController
@RequestMapping("/api/target-path")
class TargetPathController(
    private val processingStorage: ProcessingStorage
) {

    /**
     * 删除指定TargetPath
     * @param paths 需要删除的TargetPath列表，支持前缀匹配删除
     */
    @DeleteMapping
    fun deleteTargetPaths(@RequestBody paths: List<String>) {
        processingStorage.deleteTargetPaths(paths, null)
    }

}