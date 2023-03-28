package xyz.shoaky.sourcedownloader.core

import org.springframework.retry.support.RetryTemplateBuilder
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.core.component.MetadataVariableProvider
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
import kotlin.io.path.exists
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * 拉在这里，后面看情况重构
 */
class SourceProcessor(
    val name: String,
    private val source: Source,
    variableProviders: List<VariableProvider>,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    private val sourceSavePath: Path,
    private val options: ProcessorConfig.Options = ProcessorConfig.Options(),
    private val processingStorage: ProcessingStorage,
) : Runnable {

    private val downloadPath by lazy { downloader.defaultDownloadPath() }

    private val sourceItemFilters: MutableList<SourceItemFilter> = mutableListOf()
    private val sourceFileFilters: MutableList<SourceFileFilter> = mutableListOf()
    private val variableProviders = variableProviders.toMutableList()
    private val runAfterCompletion: MutableList<RunAfterCompletion> = mutableListOf()

    private val fileSavePathPattern: PathPattern = options.fileSavePathPattern
        ?: PathPattern.ORIGIN
    private val filenamePattern: PathPattern = options.filenamePattern
        ?: PathPattern.ORIGIN

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
        addItemFilter(SourceHashingItemFilter(name, processingStorage))

        if (options.provideMetadataVariables) {
            if (this.variableProviders.contains(MetadataVariableProvider).not()) {
                this.variableProviders.add(MetadataVariableProvider)
            }
        }
    }

    private fun info(): Map<String, Any> {
        return mapOf(
            "Processor" to name,
            "Source" to source::class.java.simpleName,
            "Providers" to variableProviders.map { it::class.simpleName },
            "Downloader" to downloader::class.java.simpleName,
            "Mover" to fileMover::class.java.simpleName,
            "SourceFilter" to sourceItemFilters.map { it::class.simpleName },
            "RunAfterCompletion" to runAfterCompletion.map { it::class.simpleName },
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "Options" to options,
            "SourceFileFilter" to options,
        )
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

    override fun run() {
        process()
    }

    fun dryRun(): List<ProcessingContent> {
        return process(true)
    }

    private fun process(dryRun: Boolean = false): List<ProcessingContent> {
        // TODO 其他异常会被吞掉，打印下日志
        val items = retry.execute<List<SourceItem>, IOException> {
            source.fetch()
        }

        val result = mutableListOf<ProcessingContent>()
        for (item in items) {
            if (sourceItemFilters.all { it.test(item) }.not()) {
                log.debug("Filtered item:{}", item)
                continue
            }
            kotlin.runCatching {
                retry.execute<ProcessingContent, IOException> {
                    val providers = variableProviders.filter { it.support(item) }.toList()
                    // TODO 后面根据优先级选出冲突的变量用的类
                    val idk = Idk(providers)
                    processItem(item, idk, dryRun)
                }
            }.onFailure {
                log.error("Processor:${name}处理失败, item:$item", it)
            }.onSuccess {
                if (dryRun) {
                    result.add(it)
                }
            }
        }
        return result
    }

    private fun processItem(sourceItem: SourceItem, idk: Idk, dryRun: Boolean = false): ProcessingContent {
        val aggrSourceGroup = idk.getAggr(sourceItem)
        val resolveFiles = downloader.resolveFiles(sourceItem)
        val filteredFiles = resolveFiles.filter { path ->
            val res = sourceFileFilters.all { it.test(path) }
            if (res.not()) {
                log.debug("Filtered file:{}", path)
            }
            res
        }

        val contents = aggrSourceGroup.sourceFiles(filteredFiles)
            .mapIndexed { index, sourceFile ->
                val sourceFileContent = SourceFileContent(
                    downloadPath.resolve(filteredFiles[index]),
                    sourceSavePath,
                    MapPatternVariables(sourceFile.patternVariables().variables()),
                    fileSavePathPattern,
                    filenamePattern,
                )
                sourceFileContent
            }
        val sourceContent = SourceContent(sourceItem, contents)

        val downloadTask = createDownloadTask(sourceItem)
        val needDownload = needDownload(sourceContent)
        if (needDownload.first && dryRun.not()) {
            // NOTE 非异步下载会阻塞
            this.downloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${sourceItem.title}")
            processingStorage.saveTargetPath(sourceContent.allTargetPaths())
            Events.post(ProcessorSubmitDownloadEvent(name, sourceItem))
        }

        var pc = ProcessingContent(name, sourceContent)
        if (downloader !is AsyncDownloader && dryRun.not()) {
            rename(sourceContent)
            pc = pc.copy(status = ProcessingContent.Status.RENAMED)
        }

        if (needDownload.first.not() && downloader is AsyncDownloader) {
            pc = pc.copy(status = needDownload.second)
        }
        if (options.saveContent && dryRun.not()) {
            processingStorage.save(pc)
        }
        return pc
    }

    private fun needDownload(sc: SourceContent): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        val targetPaths = files.map { it.targetPath() }
        if (targetPaths.map { it.exists() }.all { it }) {
            return false to ProcessingContent.Status.TARGET_ALREADY_EXISTS
        }
        if (processingStorage.targetPathExists(targetPaths)) {
            return false to ProcessingContent.Status.TARGET_ALREADY_EXISTS
        }

        val current = files.map { it.fileDownloadPath.exists() }
        if (current.all { it }) {
            return false to ProcessingContent.Status.WAITING_TO_RENAME
        }
        val any = current.any { it.not() }
        if (any) {
            return true to ProcessingContent.Status.WAITING_TO_RENAME
        }
        return false to ProcessingContent.Status.DOWNLOADED
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

    fun addItemFilter(vararg filters: SourceItemFilter) {
        sourceItemFilters.addAll(filters)
    }

    fun addFileFilter(vararg filters: SourceFileFilter) {
        sourceFileFilters.addAll(filters)
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
        if (sourceFiles.isEmpty()) {
            return true
        }

        if (options.renameMode == RenameMode.HARD_LINK) {
            sourceFiles.forEach {
                val targetFilePath = it.targetPath()
                Files.createLink(targetFilePath, it.fileDownloadPath)
            }
            return true
        }
        return fileMover.rename(content.copy(sourceFiles = sourceFiles))
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

    private class SourceHashingItemFilter(val sourceName: String, val processingStorage: ProcessingStorage) : SourceItemFilter {
        override fun test(item: SourceItem): Boolean {
            val processingContent = processingStorage.findByNameAndHash(sourceName, item.hashing())
            if (processingContent != null) {
                if (log.isDebugEnabled) {
                    log.debug("Source:${sourceName}已提交过下载不做处理，item:${Jackson.toJsonString(item)}")
                }
            }
            return processingContent == null
        }
    }

    override fun toString(): String {
        return info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n")
    }

}

class Idk(
    private val providers: List<VariableProvider>) {

    fun getAggr(sourceItem: SourceItem): SourceItemGroupAggr {
        val associateBy = providers.associateBy({
            it::class.simpleName!!
        }, { it.createSourceGroup(sourceItem) })
        return SourceItemGroupAggr(associateBy)
    }
}

class SourceItemGroupAggr(
    private val groups: Map<String, SourceItemGroup>
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        val res = mutableListOf<List<SourceFile>>()
        for (group in groups) {
            val sourceFiles = group.value.sourceFiles(paths)
            res.add(sourceFiles)
        }
        return List(paths.size) { index ->
            val sourceFiles = res.map { it[index] }
            sourceFiles.reduce(this::combine)
        }
    }

    private fun combine(sf1: SourceFile, sf2: SourceFile): SourceFile {
        return CombineSourceFile(sf1, sf2)
    }

    class CombineSourceFile(
        private val first: SourceFile,
        private val second: SourceFile
    ) : SourceFile {

        private val patternVariables = run {
            val pv1 = first.patternVariables()
            val pv2 = second.patternVariables()
            val var1 = pv1.variables()
            val var2 = pv2.variables()
            // TODO 处理冲突的情况，优先级
            val allVariables = var1 + var2
            MapPatternVariables(allVariables)
        }

        override fun patternVariables(): PatternVariables {
            return patternVariables
        }
    }
}