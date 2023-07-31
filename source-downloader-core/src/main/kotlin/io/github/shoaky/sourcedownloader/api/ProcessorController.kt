package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.ProcessorConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/processor")
private class ProcessorController(
    private val processorManager: ProcessorManager,
    private val configStorages: List<ProcessorConfigStorage>,
    private val configOperator: ConfigOperator
) {

    @GetMapping
    fun getProcessors(): Any {
        val processors = processorManager.getProcessors()
        return processors.map {
            ProcessorInfo(it.name, it.get().info())
        }
    }

    @GetMapping("/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig? {
        return configStorages.flatMap { it.getAllProcessorConfig() }
            .firstOrNull { it.name == processorName }
    }

    @PostMapping("/{processorName}")
    fun create(
        @PathVariable processorName: String,
        @RequestBody processorConfig: ProcessorConfig
    ) {
        val processor = processorManager.getProcessor(processorName)
        if (processor != null) {
            throw ComponentException.processorExists("processor $processorName already exists")
        }
        configOperator.save(processorName, processorConfig)
        processorManager.createProcessor(processorConfig)
    }

    @PutMapping("/{processorName}")
    fun reload(
        @PathVariable processorName: String,
        @RequestBody processorConfig: ProcessorConfig
    ) {
        processorManager.getProcessor(processorName)
            ?: throw ComponentException.processorMissing("processor $processorName not found")

        configOperator.save(processorName, processorConfig)
        processorManager.destroy(processorName)
        processorManager.createProcessor(processorConfig)
    }

    @DeleteMapping("/{processorName}")
    fun delete(@PathVariable processorName: String) {
        processorManager.destroy(processorName)
        configOperator.deleteProcessor(processorName)
    }

    @GetMapping("/dry-run/{processorName}")
    fun dryRun(@PathVariable processorName: String): List<DryRunResult> {
        val sourceProcessor = (processorManager.getProcessor(processorName)
            ?: throw ComponentException.processorMissing("Processor $processorName not found"))
        return sourceProcessor.get().dryRun()
            .map { pc ->
                val fileResult = pc.itemContent.sourceFiles.map { file ->
                    FileResult(
                        file.fileDownloadPath.toString(),
                        file.targetPath().toString(),
                        file.patternVariables.variables(),
                        file.tags,
                        file.status,
                        file.errors.joinToString(" ")
                    )
                }
                val itemContent = pc.itemContent
                val variables = itemContent.sharedPatternVariables.variables()
                DryRunResult(
                    itemContent.sourceItem, variables,
                    fileResult, pc.status.name
                )
            }
    }

}

private data class ProcessorInfo(
    val name: String,
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
    val variables: Map<String, Any>,
    val tags: Collection<String>,
    val status: FileContentStatus,
    val error: String? = null
)