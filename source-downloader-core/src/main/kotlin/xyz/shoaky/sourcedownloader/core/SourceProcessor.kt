package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import kotlin.io.path.exists

/**
 * 拉在这里，后面看情况重构
 */
class SourceProcessor(
    val name: String,
    private val source: Source,
    private val sourceContentCreator: SourceContentCreator,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    private val renameMode: RenameMode = RenameMode.MOVE,
    private val sourceSavePath: Path,
    private val options: ProcessorConfig.Options = ProcessorConfig.Options(),
    private val processingStorage: ProcessingStorage,
) : Runnable {

    private val downloadPath by lazy { downloader.defaultDownloadPath() }

    private val sourceFilters: MutableList<Predicate<SourceItem>> = mutableListOf()
    private val runAfterCompletion: MutableList<RunAfterCompletion> = mutableListOf()

    private val fileSavePathPattern: PathPattern = options.fileSavePathPattern
        ?: sourceContentCreator.defaultSavePathPattern()
    private val filenamePattern: PathPattern = options.filenamePattern
        ?: sourceContentCreator.defaultFilenamePattern()

    private var renameTaskFuture: ScheduledFuture<*>? = null

    init {
        addItemFilter(SourceHashingFilter(name, processingStorage))
    }

    fun printProcessorInfo() {
        info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n").let {
            log.info("\n初始化完成\n$it")
        }
    }

    fun scheduleRenameTask(interval: Duration) {
        if (downloader !is AsyncDownloader) {
            return
        }
        renameTaskFuture?.cancel(false)
        renameTaskFuture = scheduledExecutor.scheduleAtFixedRate({
            log.debug("processor:${name} 开始重命名任务...")
            runRenameTask()
            log.debug("processor:${name} 重命名任务完成...")
        }, 5L, interval.seconds, TimeUnit.SECONDS)
    }

    private fun info(): Map<String, Any> {
        return mapOf(
            "Processor" to name,
            "Source" to source::class.java.simpleName,
            "Creator" to sourceContentCreator::class.java.simpleName,
            "Downloader" to downloader::class.java.simpleName,
            "Mover" to fileMover::class.java.simpleName,
            "RenameMode" to renameMode,
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "SourceFilter" to sourceFilters.map { it::class.simpleName },
            "FileSavePathPattern" to fileSavePathPattern.pattern,
            "FilenamePattern" to filenamePattern.pattern,
        )
    }

    override fun run() {
        source.fetch()
            .filter { item -> sourceFilters.all { it.test(item) } }
            .map {
                it to sourceContentCreator.createSourceGroup(it)
            }.forEach { pair ->
                val sourceGroup = pair.second
                val sourceItem = pair.first
                kotlin.runCatching {
                    process(sourceItem, sourceGroup)
                }.onFailure {
                    log.error("Source:${source}文件处理失败, content:$sourceGroup", it)
                }
            }
    }

    private fun process(sourceItem: SourceItem, sourceGroup: SourceGroup) {
        val resolveFiles = downloader.resolveFiles(sourceItem)
        val contents = sourceGroup.sourceFiles(resolveFiles)
            .map {
                SourceFileContent(
                    it.downloadSavePath(downloadPath),
                    sourceSavePath,
                    it.patternVars(),
                    fileSavePathPattern,
                    filenamePattern)
            }

        val downloadOptions = DownloadOptions(options.downloadCategory)
        val downloadTask: DownloadTask = sourceGroup.createDownloadTask(downloadPath, downloadOptions)
        val needDownload = needDownload(contents)
        if (needDownload) {
            this.downloader.submit(downloadTask)
            log.debug(" 提交下载任务成功, content:$sourceGroup")
        }

        val sourceContent = SourceContent(sourceItem, contents)
        if (downloader is AsyncDownloader) {
            saveRenameTask(sourceContent, downloadTask)
        } else {
            val success = rename(sourceContent, downloadTask.torrentHash)
            if (success) {
                runDownloadCompleted(sourceContent)
            } else {
                log.warn("Processor:${name}文件命名失败, content:$sourceGroup")
            }
        }
    }

    private fun needDownload(contents: List<SourceFileContent>): Boolean {
        val target = contents.map { it.targetFilePath().exists() }
        if (target.all { it }) {
            return false
        }
        val current = contents.map { it.fileDownloadPath.exists() }
        if (current.all { it }) {
            return false
        }

        return current.any { it.not() }
    }

    private fun runDownloadCompleted(taskContent: SourceContent) {
        for (task in runAfterCompletion) {
            task.runCatching {
                this.accept(taskContent)
            }.onFailure {
                log.error("", it)
            }
        }
    }

    private fun saveRenameTask(
        content: SourceContent,
        task: DownloadTask,
    ) {
        val renameTaskRecord = ProcessingContent(
            name,
            content.sourceItem.hashing(),
            content,
            task,
        )
        processingStorage.saveRenameTask(renameTaskRecord)
    }

    private fun runRenameTask() {
        val asyncDownloader = downloader as? AsyncDownloader
        if (asyncDownloader == null) {
            log.debug("Processor:${name} 非异步下载器不执行重命名任务")
            return
        }
        processingStorage.findRenameContent(name, 3)
            .filter {
                asyncDownloader.isFinished(it.downloadTask)
            }
            .forEach {
                processRenameTask(it)
            }
    }

    private fun processRenameTask(record: ProcessingContent) {
        val processingContent = record.sourceContent
        val sourceFiles = processingContent.sourceFiles
        if (sourceFiles.all { it.targetFilePath().exists() }) {
            log.debug("全部目标文件已存在，无需重命名，record:${Jackson.toJsonString(record)}")
            return
        }

        val allSuccess = rename(processingContent, record.downloadTask.torrentHash)
        if (allSuccess) {
            runDownloadCompleted(processingContent)
        } else {
            log.warn("有部分文件重命名失败record:${Jackson.toJsonString(record)}")
        }

        if (record.renameTimes == 3) {
            log.error("重命名3次重试失败record:${Jackson.toJsonString(record)}")
        }

        val toUpdate = record.copy(
            renameTimes = record.renameTimes.inc(),
            modifyTime = LocalDateTime.now()
        )
        toUpdate.id = record.id
        processingStorage.saveRenameTask(toUpdate)
    }

    fun addItemFilter(vararg filters: Predicate<SourceItem>) {
        sourceFilters.addAll(filters)
    }

    fun safeTask(): Runnable {
        return SafeRunner(this)
    }

    private fun rename(content: SourceContent, torrentHash: String?): Boolean {
        val sourceFiles = content.canRenameFiles()
        sourceFiles.forEach {
            it.createSaveDirectories()
        }

        if (renameMode == RenameMode.HARD_LINK) {
            sourceFiles.forEach {
                Files.createLink(it.fileDownloadPath, it.targetFilePath())
            }
            return true
        }
        return fileMover.rename(sourceFiles, torrentHash)
    }

    fun addRunAfterCompletion(vararg completion: RunAfterCompletion) {
        runAfterCompletion.addAll(completion)
    }

    companion object {
        private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    }

    private class SafeRunner(private val processor: SourceProcessor) : Runnable {

        @Volatile
        private var running = false
        override fun run() {
            val name = processor.name
            log.debug("Processor:${name}处理器触发...")
            if (running) {
                log.info("Processor:${name}上一次任务还未完成，跳过本次任务")
                return
            }
            running = true
            try {
                processor.run()
            } catch (e: Exception) {
                log.error("Processor:${name}处理器执行失败", e)
            } finally {
                running = false
            }
        }
    }

    private class SourceHashingFilter(val sourceName: String, val processingStorage: ProcessingStorage) : SourceFilter {
        override fun test(item: SourceItem): Boolean {
            val processingContent = processingStorage.findByNameAndHash(sourceName, item.hashing())
            if (processingContent != null) {
                log.debug("Source:${sourceName}已提交过下载不做处理，item:${Jackson.toJsonString(item)}")
            }
            return processingContent == null
        }

    }

}