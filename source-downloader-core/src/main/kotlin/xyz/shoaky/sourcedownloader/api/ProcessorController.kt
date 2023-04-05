package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.core.ProcessorConfig
import xyz.shoaky.sourcedownloader.core.ProcessorConfigStorage
import xyz.shoaky.sourcedownloader.core.SdComponentManager
import xyz.shoaky.sourcedownloader.sdk.SourceItem

@RestController
@RequestMapping("/api/processor")
class ProcessorController(
    private val componentManager: SdComponentManager,
    private val configStorages: List<ProcessorConfigStorage>
) {

    @GetMapping("/config/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig? {
        return configStorages.flatMap { it.getAllProcessor() }
            .firstOrNull { it.name == processorName }
    }

    @GetMapping("/dry-run/{processorName}")
    fun dryRun(@PathVariable processorName: String): List<DryRunResult> {
        val sourceProcessor = (componentManager.getProcessor(processorName)
            ?: throw IllegalArgumentException("processor $processorName not found"))
        return sourceProcessor.dryRun()
            .map { pc ->
                val fileResult = pc.sourceContent.sourceFiles.map { file ->
                    mapOf(
                        "from" to "${file.fileDownloadPath}}",
                        "to" to "${file.targetPath()}",
                        "variables" to file.patternVariables.variables(),
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