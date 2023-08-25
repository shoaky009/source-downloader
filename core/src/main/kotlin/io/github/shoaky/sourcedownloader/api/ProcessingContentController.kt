package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/processing-content")
private class ProcessingContentController(
    private val storage: ProcessingStorage
) {

    @GetMapping("/{id}")
    fun getProcessingContent(@PathVariable id: Long): ProcessingContent? {
        return storage.findById(id)
    }

    @PutMapping("/{id}")
    fun modifyProcessingContent(@PathVariable id: Long, @RequestBody body: ProcessingContent): ProcessingContent {
        val current = storage.findById(id) ?: throw NotFoundException(
            "ProcessingContent with id $id not found"
        )
        return storage.save(
            current.copy(
                // 这个待定，有些字段不允许修改
                itemContent = body.itemContent,
                renameTimes = body.renameTimes,
                modifyTime = LocalDateTime.now()
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteProcessingContent(@PathVariable id: Long) {
        storage.deleteProcessingContent(id)
    }
}