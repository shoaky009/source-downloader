package xyz.shoaky.sourcedownloader.core

import org.springframework.retry.support.RetryTemplateBuilder
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import xyz.shoaky.sourcedownloader.util.Events
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import kotlin.io.path.exists
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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

    private val downloadOptions = DownloadOptions(
        options.downloadCategory
    )

    private val safeRunner by lazy {
        SafeRunner(this)
    }

    private val retry = RetryTemplateBuilder()
        .maxAttempts(3)
        .fixedBackoff(Duration.ofSeconds(5L).toMillis())
        .build()

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

    @OptIn(ExperimentalTime::class)
    fun scheduleRenameTask(interval: Duration) {
        if (downloader !is AsyncDownloader) {
            return
        }
        renameTaskFuture?.cancel(false)
        renameTaskFuture = scheduledExecutor.scheduleAtFixedRate({
            log.debug("Processor:${name} 开始重命名任务...")
            val measureTime = measureTime {
                try {
                    runRenameTask()
                } catch (e: Exception) {
                    log.error("Processor:${name} 重命名任务出错", e)
                }
                System.currentTimeMillis()
            }
            log.info("Processor:${name} 重命名任务完成 took:${measureTime.inWholeMilliseconds}ms")
        }, 5L, interval.seconds, TimeUnit.SECONDS)
    }

    private fun info(): Map<String, Any> {
        return mapOf(
            "Processor" to name,
            "Source" to source::class.java.simpleName,
            "Creator" to sourceContentCreator::class.java.simpleName,
            "Downloader" to downloader::class.java.simpleName,
            "Mover" to fileMover::class.java.simpleName,
            "SourceFilter" to sourceFilters.map { it::class.simpleName },
            "RunAfterCompletion" to runAfterCompletion.map { it::class.simpleName },
            "RenameMode" to renameMode,
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "FileSavePathPattern" to fileSavePathPattern.pattern,
            "FilenamePattern" to filenamePattern.pattern,
        )
    }

    override fun run() {
        // TODO 其他异常会被吞掉，打印下日志
        val items = retry.execute<List<SourceItem>, IOException> {
            source.fetch()
        }

        for (item in items) {
            if (sourceFilters.all { it.test(item) }.not()) {
                log.debug("Filtered item:$item")
                continue
            }
            kotlin.runCatching {
                retry.execute<Unit, IOException> {
                    val sourceGroup = sourceContentCreator.createSourceGroup(item)
                    process(item, sourceGroup)
                }
            }.onFailure { log.error("Processor:${name}处理失败, item:$item", it) }
        }
    }

    private fun process(sourceItem: SourceItem, sourceGroup: SourceGroup) {
        val resolveFiles = downloader.resolveFiles(sourceItem)
        val contents = sourceGroup.sourceFiles(resolveFiles)
            .mapIndexed { index, sourceFile ->
                SourceFileContent(
                    downloadPath.resolve(resolveFiles[index]),
                    sourceSavePath,
                    MapPatternVariables(sourceFile.patternVariables().getVariables()),
                    fileSavePathPattern,
                    filenamePattern
                )
            }
        val sourceContent = SourceContent(sourceItem, contents)

        val downloadTask = createDownloadTask(sourceItem)
        val needDownload = needDownload(sourceContent)
        if (needDownload) {
            // NOTE 非异步下载会阻塞
            this.downloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${sourceItem.title}")
            processingStorage.saveTargetPath(sourceContent.allTargetPaths())
            Events.post(ProcessorSubmitDownloadEvent(name, sourceItem))
        }

        var pc = ProcessingContent(name, sourceContent)
        if (downloader !is AsyncDownloader) {
            rename(sourceContent)
            pc = ProcessingContent(name, sourceContent)
                .copy(status = ProcessingContent.Status.RENAMED)
        }
        processingStorage.save(pc)
    }

    private fun needDownload(sc: SourceContent): Boolean {
        val files = sc.sourceFiles
        val targetPaths = files.map { it.targetPath() }
        if (targetPaths.map { it.exists() }.all { it }) {
            return false
        }
        if (processingStorage.targetPathExists(targetPaths)) {
            return false
        }

        val current = files.map { it.fileDownloadPath.exists() }
        if (current.all { it }) {
            return false
        }
        return current.any { it.not() }
    }

    private fun runAfterCompletions(taskContent: SourceContent) {
        for (task in runAfterCompletion) {
            task.runCatching {
                this.accept(taskContent)
            }.onFailure {
                log.error("${task::class.simpleName}发生错误", it)
            }
        }
    }

    private enum class DownloadStatus {
        FINISHED,
        NOT_FINISHED,
        NOT_FOUND;

        companion object {
            fun from(boolean: Boolean?): DownloadStatus {
                return when (boolean) {
                    true -> FINISHED
                    false -> NOT_FINISHED
                    null -> NOT_FOUND
                }
            }
        }
    }

    private fun runRenameTask() {
        val asyncDownloader = downloader as? AsyncDownloader
        if (asyncDownloader == null) {
            log.debug("Processor:${name} 非异步下载器不执行重命名任务")
            return
        }
        val contentGrouping = processingStorage.findRenameContent(name, options.renameTimesThreshold)
            .groupBy(
                {
                    val downloadTask = createDownloadTask(it.sourceContent.sourceItem)
                    DownloadStatus.from(asyncDownloader.isFinished(downloadTask))
                }, { it }
            )

        contentGrouping[DownloadStatus.NOT_FOUND]?.forEach { pc ->
            kotlin.runCatching {
                log.info("Processing下载任务不存在, record:${Jackson.toJsonString(pc)}")
                processingStorage.save(pc.copy(
                    status = ProcessingContent.Status.DOWNLOAD_FAILED,
                    modifyTime = LocalDateTime.now(),
                ))
            }.onFailure {
                log.error("Processing更新状态出错, record:${Jackson.toJsonString(pc)}", it)
            }
        }

        contentGrouping[DownloadStatus.FINISHED]?.forEach { pc ->
            kotlin.runCatching {
                processRenameTask(pc)
            }.onFailure {
                log.error("Processing重命名任务出错, record:${Jackson.toJsonString(pc)}", it)
            }
        }
    }

    private fun processRenameTask(content: ProcessingContent) {
        val processingContent = content.sourceContent
        val sourceFiles = processingContent.sourceFiles
        if (sourceFiles.all { it.targetPath().exists() }) {
            val toUpdate = content.copy(
                renameTimes = content.renameTimes.inc(),
                status = ProcessingContent.Status.TARGET_ALREADY_EXISTS,
                modifyTime = LocalDateTime.now(),
            )
            processingStorage.save(toUpdate)
            log.info("全部目标文件已存在，无需重命名，record:${Jackson.toJsonString(content)}")
            return
        }

        val allSuccess = rename(processingContent)
        if (allSuccess) {
            val paths = processingContent.sourceFiles.map { it.targetPath() }
            // 如果失败了, 一些成功一些失败??
            processingStorage.saveTargetPath(paths)
            runAfterCompletions(processingContent)
        } else {
            log.warn("有部分文件重命名失败record:${Jackson.toJsonString(content)}")
        }

        val renameTimesThreshold = options.renameTimesThreshold
        if (content.renameTimes == renameTimesThreshold) {
            log.error("重命名${renameTimesThreshold}次重试失败record:${Jackson.toJsonString(content)}")
        }

        val toUpdate = content.copy(
            renameTimes = content.renameTimes.inc(),
            status = ProcessingContent.Status.RENAMED,
            modifyTime = LocalDateTime.now()
        )
        processingStorage.save(toUpdate)
    }

    fun addItemFilter(vararg filters: Predicate<SourceItem>) {
        sourceFilters.addAll(filters)
    }

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(sourceItem: SourceItem): DownloadTask {
        return DownloadTask.create(sourceItem, downloadPath = downloadPath, category = downloadOptions.category)
    }

    private fun rename(content: SourceContent): Boolean {
        val sourceFiles = content.canRenameFiles()
        sourceFiles.forEach {
            it.createSaveDirectories()
        }

        if (renameMode == RenameMode.HARD_LINK) {
            sourceFiles.forEach {
                val targetFilePath = it.targetPath()
                Files.createLink(it.fileDownloadPath, targetFilePath)
            }
            return true
        }
        return fileMover.rename(content)
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
            log.info("Processor:${name} 处理器触发获取源信息")
            if (running) {
                log.info("Processor:${name} 上一次任务还未完成，跳过本次任务")
                return
            }
            running = true
            try {
                processor.run()
            } catch (e: Exception) {
                log.error("Processor:${name} 处理器执行失败", e)
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