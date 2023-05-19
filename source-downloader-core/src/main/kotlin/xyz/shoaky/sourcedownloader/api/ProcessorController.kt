package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.*
import xyz.shoaky.sourcedownloader.core.ConfigOperator
import xyz.shoaky.sourcedownloader.core.ProcessorConfig
import xyz.shoaky.sourcedownloader.core.ProcessorConfigStorage
import xyz.shoaky.sourcedownloader.core.ProcessorManager
import xyz.shoaky.sourcedownloader.core.file.FileContentStatus
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentException

@RestController
@RequestMapping("/api/processor")
private class ProcessorController(
    private val processorManager: ProcessorManager,
    private val configStorages: List<ProcessorConfigStorage>,
    private val configOperator: ConfigOperator
) {

    @GetMapping
    fun processors(): Any {
        val processors = processorManager.getProcessors()
        return processors.map {
            ProcessorInfo(it.name, it.info())
        }
    }

    @GetMapping("/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig? {
        return configStorages.flatMap { it.getAllProcessorConfig() }
            .firstOrNull { it.name == processorName }
    }


    @PostMapping("/{processorName}")
    fun create(@PathVariable processorName: String,
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
    fun reload(@PathVariable processorName: String,
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
            ?: throw ComponentException.processorMissing("processor $processorName not found"))
        return sourceProcessor.dryRun()
            .map { pc ->
                val fileResult = pc.sourceContent.sourceFiles.map { file ->
                    FileResult(
                        file.fileDownloadPath.toString(),
                        file.targetPath().toString(),
                        file.patternVariables.variables(),
                        file.tags(),
                        file.status
                    )
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
    val tags: List<String>,
    val status: FileContentStatus
)