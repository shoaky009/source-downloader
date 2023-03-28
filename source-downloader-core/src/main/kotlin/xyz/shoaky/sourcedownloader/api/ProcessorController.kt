package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.core.ComponentManagerV2
import xyz.shoaky.sourcedownloader.sdk.SourceItem

@RestController
@RequestMapping("/api/processor")
class ProcessorController(
    private val componentManager: ComponentManagerV2
) {

    fun getProcessors() {

    }

    @GetMapping("/dry-run/{processorName}")
    fun dryRun(@PathVariable processorName: String): List<DryRunResult> {
        val sourceProcessor = (componentManager.getProcessor(processorName)
            ?: throw IllegalArgumentException("processor $processorName not found"))
        return sourceProcessor.dryRun()
            .map { pc ->
                val fileResult = pc.sourceContent.sourceFiles.map { file ->
                    mapOf(
                        "path" to "${file.fileDownloadPath} ---> ${file.targetPath()}",
                        "variables" to file.patternVariables.getVariables(),
                    )
                }
                DryRunResult(pc.sourceContent.sourceItem, fileResult, pc.status.name)
            }
    }
}

data class DryRunResult(
    val sourceItem: SourceItem,
    val fileResult: List<Map<String, Any>>,
    val status: String
)