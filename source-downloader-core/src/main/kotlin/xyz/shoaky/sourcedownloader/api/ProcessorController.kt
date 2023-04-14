package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.core.ProcessorConfigStorage
import xyz.shoaky.sourcedownloader.core.ProcessorManager
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig
import xyz.shoaky.sourcedownloader.core.processor.ProcessorStatus
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentException

@RestController
@RequestMapping("/api/processor")
private class ProcessorController(
    private val processorManager: ProcessorManager,
    private val configStorages: List<ProcessorConfigStorage>
) {

    @GetMapping("/all")
    fun processors(): Any {
        val processors = processorManager.getProcessors()
        return processors.map {
            ProcessorInfo(it.name, it.getStatus(), it.info())
        }
    }

    @GetMapping("/config/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig? {
        return configStorages.flatMap { it.getAllProcessorConfig() }
            .firstOrNull { it.name == processorName }
    }

    @GetMapping("/dry-run/{processorName}")
    fun dryRun(@PathVariable processorName: String): List<DryRunResult> {
        val sourceProcessor = (processorManager.getProcessor(processorName)
            ?: throw ComponentException.processorMissing("processor $processorName not found"))
        return sourceProcessor.dryRun()
            .map { pc ->
                val fileResult = pc.sourceContent.sourceFiles.map { file ->
                    FileResult(file.fileDownloadPath.toString(), file.targetPath().toString(), file.patternVariables.variables())
                }
                val sourceContent = pc.sourceContent
                val variables = sourceContent.sharedPatternVariables.variables()
                DryRunResult(sourceContent.sourceItem, variables,
                    fileResult, pc.status.name)
            }
    }

}

private data class ProcessorInfo(
    val name: String,
    val status: ProcessorStatus,
    val components: Map<String, Any>
)

private data class DryRunResult(
    val sourceItem: SourceItem,
    val sharedVariables: Map<String, Any>,
    val fileResults: List<FileResult>,
    val status: String
)

private data class FileResult(
    val from: String,
    val to: String,
    val variables: Map<String, Any>
)