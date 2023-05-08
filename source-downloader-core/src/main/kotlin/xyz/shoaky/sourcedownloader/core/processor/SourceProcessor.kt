package xyz.shoaky.sourcedownloader.core.processor

import org.springframework.retry.support.RetryTemplateBuilder
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.component.provider.MetadataVariableProvider
import xyz.shoaky.sourcedownloader.core.*
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import xyz.shoaky.sourcedownloader.util.Events
import java.io.IOException
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
    // 先这样传入 后面要改
    private val sourceId: String,
    private val source: Source<SourceItemPointer>,
    variableProviders: List<VariableProvider>,
    private val itemFileResolver: ItemFileResolver,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    private val sourceSavePath: Path,
    private val options: ProcessorConfig.Options = ProcessorConfig.Options(),
    private val processingStorage: ProcessingStorage,
) : Runnable {

    private val sourceItemFilters: MutableList<SourceItemFilter> = mutableListOf()
    private val sourceFileFilters: MutableList<SourceFileFilter> = mutableListOf()
    private val variableProviders = variableProviders.toMutableList()
    private val runAfterCompletion: MutableList<RunAfterCompletion> = mutableListOf()
    private val taggers: MutableList<FileTagger> = mutableListOf()

    private val downloadPath = downloader.defaultDownloadPath()
    private val fileSavePathPattern: PathPattern = options.savePathPattern
    private val filenamePattern: PathPattern = options.filenamePattern

    private var renameTaskFuture: ScheduledFuture<*>? = null
    private val safeRunner by lazy {
        SafeRunner(this)
    }

    private var status: ProcessorStatus = ProcessorStatus.RUNNING

    fun getStatus(): ProcessorStatus {
        return status
    }

    init {
        if (options.saveContent) {
            addItemFilter(SourceHashingItemFilter(name, processingStorage))
        }
        if (options.provideMetadataVariables) {
            if (this.variableProviders.map { it }.contains(MetadataVariableProvider).not()) {
                this.variableProviders.add(MetadataVariableProvider)
            }
        }
    }

    fun info(): Map<String, Any> {
        return mapOf(
            "Processor" to name,
            "Source" to source::class.java.simpleName,
            "Providers" to variableProviders.map { it::class.simpleName },
            "Resolver" to itemFileResolver::class.java.simpleName,
            "Downloader" to downloader::class.java.simpleName,
            "Mover" to fileMover::class.java.simpleName,
            "SourceItemFilter" to sourceItemFilters.map { it::class.simpleName },
            "RunAfterCompletion" to runAfterCompletion.map { it::class.simpleName },
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "SourceFileFilter" to sourceFileFilters.map { it::class.simpleName },
            "Taggers" to taggers.map { it::class.simpleName },
            "Options" to options,
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
            var modified = false
            val measureTime = measureTime {
                try {
                    modified = runRename() > 0
                } catch (e: Exception) {
                    log.error("Processor:${name} 重命名任务出错", e)
                }
                System.currentTimeMillis()
            }

            if (modified) {
                log.info("Processor:${name} 重命名任务完成 took:${measureTime.inWholeMilliseconds}ms")
            }
            if (modified.not() && measureTime.inWholeMilliseconds > 50L) {
                log.warn("Processor:${name} 重命名任务没有修改 took:${measureTime.inWholeMilliseconds}ms")
            }
        }, 5L, interval.seconds, TimeUnit.SECONDS)
    }

    override fun run() {
        process()
    }

    fun dryRun(): List<ProcessingContent> {
        val contents = process(true)
        contents.forEach {
            it.sourceContent.updateFileStatus(fileMover)
        }
        return contents
    }

    private fun process(dryRun: Boolean = false): List<ProcessingContent> {
        val lastState = processingStorage.findProcessorSourceState(name, sourceId)
        val itemIterator = retry.execute<Iterator<PointedItem<SourceItemPointer>>, IOException> {
            val pointer = lastState?.resolvePointer(source::class)
            val iterator = source.fetch(pointer).iterator()
            iterator
        }

        val result = mutableListOf<ProcessingContent>()
        // 好像没什么用，后面处理器状态改查failed状态的
        var failure = false
        var lastPointedItem: PointedItem<SourceItemPointer>? = null
        for (item in itemIterator) {
            val filterBy = sourceItemFilters.firstOrNull { it.test(item.sourceItem).not() }
            if (filterBy != null) {
                log.debug("{} Filtered item:{}", filterBy::class.simpleName, item)
                continue
            }
            kotlin.runCatching {
                retry.execute<ProcessingContent, IOException> {
                    processItem(item.sourceItem, dryRun)
                }
            }.onFailure {
                log.error("Processor:${name} 处理失败, item:$item", it)
                status = ProcessorStatus.ERROR
                failure = true
            }.onSuccess {
                if (dryRun) {
                    result.add(it)
                }
            }
            lastPointedItem = item
        }
        if (!failure) {
            status = ProcessorStatus.RUNNING
        }
        if (dryRun.not()) {
            saveSourceState(lastPointedItem, lastState)
        }
        return result
    }

    private fun saveSourceState(lastPointedItem: PointedItem<SourceItemPointer>?, lastState: ProcessorSourceState?) {
        if (lastState != null && lastPointedItem == null) {
            processingStorage.save(
                lastState.copy(lastActiveTime = LocalDateTime.now())
            )
            return
        }
        if (lastPointedItem == null) {
            log.info("Processor:${name} Source:${sourceId} no items to process")
            return
        }
        val latestPointer = Jackson.convert(lastPointedItem.pointer, PersistentItemPointer::class)
        val sourceState = lastState?.copy(
            processorName = name,
            sourceId = sourceId,
            lastPointer = latestPointer,
            lastActiveTime = LocalDateTime.now()
        ) ?: ProcessorSourceState(
            processorName = name,
            sourceId = sourceId,
            lastPointer = latestPointer,
        )
        processingStorage.save(sourceState)
    }

    private fun processItem(sourceItem: SourceItem, dryRun: Boolean = false): ProcessingContent {
        val variablesAggregation = VariableProvidersAggregation(
            sourceItem,
            variableProviders.filter { it.support(sourceItem) }.toList(),
            options.variableConflictStrategy,
            options.variableNameReplace
        )
        val sourceContent = createPersistentSourceContent(variablesAggregation, sourceItem)

        val downloadTask = createDownloadTask(
            sourceItem,
            sourceContent.sourceFiles.map { it.fileDownloadPath }
        )
        val needDownload = needDownload(sourceContent)

        if (needDownload.first && dryRun.not()) {
            // NOTE 非异步下载会阻塞
            this.downloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${sourceItem.title}")
            processingStorage.saveTargetPath(sourceContent.allTargetPaths())
            Events.post(ProcessorSubmitDownloadEvent(name, sourceContent))
        }

        var status = ProcessingContent.Status.WAITING_TO_RENAME
        if (downloader !is AsyncDownloader && dryRun.not()) {
            rename(sourceContent)
            status = ProcessingContent.Status.RENAMED
        }

        if (needDownload.first.not() && downloader is AsyncDownloader) {
            status = needDownload.second
        }

        val pc = ProcessingContent(name, sourceContent).copy(status = status)
        if (options.saveContent && dryRun.not()) {
            processingStorage.save(pc)
        }
        return pc
    }

    private fun createPersistentSourceContent(sourceItemGroup: SourceItemGroup, sourceItem: SourceItem): PersistentSourceContent {
        val resolveFiles = itemFileResolver.resolveFiles(sourceItem)
        val filteredFiles = resolveFiles.filter { path ->
            val res = sourceFileFilters.all { it.test(path) }
            if (res.not()) {
                log.debug("Filtered file:{}", path)
            }
            res
        }

        val sourceFiles = sourceItemGroup.sourceFiles(filteredFiles)
            .mapIndexed { index, sourceFile ->
                val sourceFileContent = CoreFileContent(
                    downloadPath.resolve(filteredFiles[index]),
                    sourceSavePath,
                    downloadPath,
                    MapPatternVariables(sourceFile.patternVariables().variables()),
                    fileSavePathPattern,
                    filenamePattern,
                )
                sourceFileContent.addSharedVariables(sourceItemGroup.sharedPatternVariables())
                tagFileAndReplaceFilenamePattern(sourceFileContent)
            }
        val variables = sourceItemGroup.sharedPatternVariables()
        return PersistentSourceContent(sourceItem, sourceFiles, MapPatternVariables(variables))
    }

    private fun tagFileAndReplaceFilenamePattern(fileContent: CoreFileContent): CoreFileContent {
        val customTags = options.tagFilenamePattern.keys
        if (customTags.isEmpty()) {
            return fileContent
        }
        val tags = taggers.mapNotNull { it.tag(fileContent) }
        fileContent.tag(tags)

        val tagged = customTags.filter {
            fileContent.isTagged(it)
        }
        log.debug("Processor:{} 文件:{} 标签:{}", name, fileContent.fileDownloadPath, tagged)
        val pathPatterns = tagged.mapNotNull {
            options.tagFilenamePattern[it]
        }
        if (pathPatterns.isEmpty()) {
            return fileContent
        }
        val typePattern = pathPatterns.first()
        log.debug("Processor:{} 文件:{} 使用自定义命名规则:{}", name, fileContent.fileDownloadPath, typePattern)
        val copy = fileContent.copy(filenamePattern = typePattern)
        copy.tag(fileContent.tags())
        return copy
    }

    private fun needDownload(sc: PersistentSourceContent): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        if (files.isEmpty()) {
            return false to ProcessingContent.Status.NO_FILES
        }

        val targetPaths = files.map { it.targetPath() }
        // 预防这一批次的Item有相同的目标，并且是AsyncDownloader的情况下会重复下载
        if (processingStorage.targetPathExists(targetPaths)) {
            return false to ProcessingContent.Status.TARGET_ALREADY_EXISTS
        }

        if (fileMover.exists(targetPaths)) {
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

    fun runRename(): Int {
        val asyncDownloader = downloader as? AsyncDownloader
        if (asyncDownloader == null) {
            log.debug("Processor:${name} 非异步下载器不执行重命名任务")
            return 0
        }
        val contentGrouping = processingStorage.findRenameContent(name, options.renameTimesThreshold)
            .groupBy(
                { pc ->
                    val files = pc.sourceContent.sourceFiles.map { it.fileDownloadPath }
                    val downloadTask = createDownloadTask(pc.sourceContent.sourceItem, files)
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
        return contentGrouping[DownloadStatus.FINISHED]?.size ?: 0
    }

    private fun processRenameTask(pc: ProcessingContent) {
        val sourceContent = pc.sourceContent
        val sourceFiles = sourceContent.sourceFiles
        sourceFiles.forEach {
            it.addSharedVariables(sourceContent.sharedPatternVariables)
        }

        val targetPaths = sourceFiles.map { it.targetPath() }
        if (fileMover.exists(targetPaths)) {
            val toUpdate = pc.copy(
                renameTimes = pc.renameTimes.inc(),
                status = ProcessingContent.Status.TARGET_ALREADY_EXISTS,
                modifyTime = LocalDateTime.now(),
            )
            processingStorage.save(toUpdate)
            log.info("全部目标文件已存在，无需重命名，record:${Jackson.toJsonString(pc)}")
            return
        }

        val allSuccess = rename(sourceContent)
        if (allSuccess) {
            val paths = sourceContent.sourceFiles.map { it.targetPath() }
            // 如果失败了, 一些成功一些失败??
            processingStorage.saveTargetPath(paths)
            runAfterCompletions(sourceContent)
        } else {
            log.warn("有部分文件重命名失败record:${Jackson.toJsonString(pc)}")
        }

        val renameTimesThreshold = options.renameTimesThreshold
        if (pc.renameTimes == renameTimesThreshold) {
            log.error("重命名${renameTimesThreshold}次重试失败record:${Jackson.toJsonString(pc)}")
        }

        val toUpdate = pc.copy(
            renameTimes = pc.renameTimes.inc(),
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

    private fun createDownloadTask(sourceItem: SourceItem, downloadFiles: List<Path>): DownloadTask {
        return DownloadTask(sourceItem, downloadFiles, downloadPath, options.downloadOptions)
    }

    private fun getRenameFiles(content: SourceContent): List<FileContent> {
        val filter = content.sourceFiles
            .filter { fileMover.exists(listOf(it.targetPath())).not() }
            .filter { it.fileDownloadPath.exists() }

        // 冲突的不移动
        val conflicts = filter.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys

        val partition = filter.partition { conflicts.contains(it.targetPath()).not() }
        if (partition.second.isNotEmpty()) {
            log.warn("存在重名文件，需要手动处理 files:${partition.second.map { it.fileDownloadPath }}")
        }
        return partition.first
    }

    private fun rename(content: PersistentSourceContent): Boolean {
        val renameFiles = content.getRenameFiles(fileMover)
        if (renameFiles.isEmpty()) {
            return true
        }
        renameFiles.map { it.saveDirectoryPath() }
            .distinct()
            .forEach {
                fileMover.createDirectories(it)
            }
        return fileMover.rename(content)
    }

    fun addRunAfterCompletion(vararg completion: RunAfterCompletion) {
        runAfterCompletion.addAll(completion)
    }

    override fun toString(): String {
        return info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n")
    }

    fun addTagger(tagger: FileTagger) {
        if (taggers.contains(tagger).not()) {
            taggers.add(tagger)
        }
    }

    companion object {
        // 随便给的
        private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
        private val retry = RetryTemplateBuilder()
            .maxAttempts(3)
            .fixedBackoff(Duration.ofSeconds(5L).toMillis())
            .build()
    }

}


private class SafeRunner(
    private val processor: SourceProcessor
) : Runnable {

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

