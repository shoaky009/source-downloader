package xyz.shoaky.sourcedownloader.core.processor

import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.util.StopWatch
import xyz.shoaky.sourcedownloader.component.provider.MetadataVariableProvider
import xyz.shoaky.sourcedownloader.core.*
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.FileContentStatus
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
    private val fileContentFilters: MutableList<FileContentFilter> = mutableListOf()
    private val variableProviders = variableProviders.toMutableList()
    private val runAfterCompletion: MutableList<RunAfterCompletion> = mutableListOf()
    private val taggers: MutableList<FileTagger> = mutableListOf()

    private val downloadPath = downloader.defaultDownloadPath()
    private val fileSavePathPattern: PathPattern = options.savePathPattern

    private val variableReplacers: MutableList<VariableReplacer> = mutableListOf(
        *options.variableReplacers.toTypedArray(), WindowsPathReplacer
    )
    private val filenamePattern: PathPattern = CorePathPattern(
        options.filenamePattern.pattern,
        variableReplacers
    )
    private val tagFilenamePattern = options.tagFilenamePattern.mapValues {
        CorePathPattern(
            it.value.pattern,
            variableReplacers
        )
    }

    private var renameTaskFuture: ScheduledFuture<*>? = null
    private val safeRunner by lazy {
        SafeRunner(this)
    }
    private val renameScheduledExecutor by lazy {
        Executors.newSingleThreadScheduledExecutor()
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
            "FileResolver" to itemFileResolver::class.java.simpleName,
            "Downloader" to downloader::class.java.simpleName,
            "FileMover" to fileMover::class.java.simpleName,
            "SourceItemFilter" to sourceItemFilters.map { it::class.simpleName },
            "RunAfterCompletion" to runAfterCompletion.map { it::class.simpleName },
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "SourceFileFilter" to fileContentFilters.map { it::class.simpleName },
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
        renameTaskFuture = renameScheduledExecutor.scheduleAtFixedRate({
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

            val renameCostTimeThreshold = 100L
            if (modified.not() && measureTime.inWholeMilliseconds > renameCostTimeThreshold) {
                log.warn("Processor:${name} 重命名任务没有修改 took:${measureTime.inWholeMilliseconds}ms")
            }
        }, 5L, interval.seconds, TimeUnit.SECONDS)
    }

    override fun run() {
        process()
    }

    fun dryRun(): List<ProcessingContent> {
        return process(true)
    }

    private fun process(dryRun: Boolean = false): List<ProcessingContent> {
        val lastState = processingStorage.findProcessorSourceState(name, sourceId)
        val itemIterator = retry.execute<Iterator<PointedItem<SourceItemPointer>>, IOException> {
            it.setAttribute("stage", ProcessStage("FetchSourceItems", lastState))
            val pointer = lastState?.resolvePointer(source::class)
            val iterator = source.fetch(pointer).iterator()
            iterator
        }

        val result = mutableListOf<ProcessingContent>()
        var lastPointedItem: PointedItem<SourceItemPointer>? = null

        val simpleStat = SimpleStat(name)
        for (item in itemIterator) {
            val filterBy = sourceItemFilters.firstOrNull { it.test(item.sourceItem).not() }
            if (filterBy != null) {
                log.debug("{} Filtered item:{}", filterBy::class.simpleName, item)
                lastPointedItem = item
                simpleStat.incFilterCounting()
                continue
            }
            val processingContent = kotlin.runCatching {
                retry.execute<ProcessingContent, Throwable> {
                    it.setAttribute("stage", ProcessStage("ProcessItem", item))
                    processItem(item.sourceItem, dryRun)
                }
            }.onFailure {
                log.error("Processor:${name}处理失败, item:$item", it)
            }.onSuccess {
                if (dryRun) {
                    result.add(it)
                }
                // 也许有一直失败的会卡住整体，暂时先这样处理
                lastPointedItem = item
            }.getOrElse {
                ProcessingContent(
                    name, PersistentSourceContent(
                    item.sourceItem, emptyList(), MapPatternVariables()
                )
                ).copy(status = ProcessingContent.Status.FAILURE, failureReason = it.message)
            }

            if (options.saveContent && dryRun.not()) {
                processingStorage.save(processingContent)
            }

            simpleStat.incProcessingCounting()
        }
        simpleStat.stopWatch.stop()

        if (simpleStat.isChanged()) {
            log.info("Processor:{}", simpleStat)
        }

        if (dryRun.not()) {
            saveSourceState(lastPointedItem, lastState)
        }
        return result
    }

    private fun saveSourceState(lastPointedItem: PointedItem<SourceItemPointer>?, lastState: ProcessorSourceState?) {
        // not first time but no items
        if (lastState != null && lastPointedItem == null) {
            processingStorage.save(
                lastState.copy(lastActiveTime = LocalDateTime.now())
            )
            return
        }

        // first time and no items
        if (lastPointedItem == null) {
            log.info("Processor:${name} Source:${sourceId} no items to process")
            return
        }

        log.info("Processor:$name update pointer Source:$sourceId lastPointedItem:$lastPointedItem")
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
        val contentStatus = probeContent(sourceContent)

        sourceContent.updateFileStatus(fileMover)
        val downloadTask = createDownloadTask(sourceContent)
        if (contentStatus.first && dryRun.not()) {
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

        if (contentStatus.first.not() && downloader is AsyncDownloader) {
            status = contentStatus.second
        }

        return ProcessingContent(name, sourceContent).copy(status = status)
    }

    private fun createPersistentSourceContent(
        sourceItemGroup: SourceItemGroup,
        sourceItem: SourceItem
    ): PersistentSourceContent {
        val resolveFiles = itemFileResolver.resolveFiles(sourceItem)
        val sharedPatternVariables = sourceItemGroup.sharedPatternVariables()
        val sourceFiles = sourceItemGroup.filePatternVariables(resolveFiles)
            .mapIndexed { index, sourceFile ->
                val resolveFile = resolveFiles[index]
                val sourceFileContent = CoreFileContent(
                    downloadPath.resolve(resolveFile.path),
                    sourceSavePath,
                    downloadPath,
                    MapPatternVariables(sourceFile.patternVariables().variables()),
                    fileSavePathPattern,
                    filenamePattern,
                    resolveFile.attributes
                )
                // 这里坑，后面看怎么改
                val tagged = tagFileAndReplaceFilenamePattern(sourceFileContent)
                tagged.setVariableErrorStrategy(options.variableErrorStrategy)
                tagged.addSharedVariables(sharedPatternVariables)
                tagged
            }.filter { path ->
                val filter = fileContentFilters.all { it.test(path) }
                if (filter.not()) {
                    log.debug("Filtered file:{}", path)
                }
                filter
            }
        return PersistentSourceContent(sourceItem, sourceFiles, MapPatternVariables(sharedPatternVariables))
    }

    private fun tagFileAndReplaceFilenamePattern(fileContent: CoreFileContent): CoreFileContent {
        val customTags = tagFilenamePattern.keys
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
        val taggedFilePattern = pathPatterns.first()
        log.debug("Processor:{} 文件:{} 使用自定义命名规则:{}", name, fileContent.fileDownloadPath, taggedFilePattern)
        val copy = fileContent.copy(filenamePattern = taggedFilePattern)
        // 考虑要不要存tag
        copy.tag(fileContent.tags())
        return copy
    }

    private fun probeContent(sc: PersistentSourceContent): Pair<Boolean, ProcessingContent.Status> {
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
                    val downloadTask = createDownloadTask(pc.sourceContent)
                    DownloadStatus.from(asyncDownloader.isFinished(downloadTask))
                }, { it }
            )

        contentGrouping[DownloadStatus.NOT_FOUND]?.forEach { pc ->
            kotlin.runCatching {
                log.info("Processing下载任务不存在, record:${Jackson.toJsonString(pc)}")
                processingStorage.save(
                    pc.copy(
                        status = ProcessingContent.Status.DOWNLOAD_FAILED,
                        modifyTime = LocalDateTime.now(),
                    )
                )
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
        val replacers = variableReplacers.toTypedArray()
        sourceFiles.forEach {
            // 这边设计不太好
            it.addSharedVariables(sourceContent.sharedPatternVariables)
            it.setVariableErrorStrategy(options.variableErrorStrategy)
            val corePathPattern = it.filenamePattern as CorePathPattern
            corePathPattern.addReplacer(*replacers)
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

    fun addFileFilter(vararg filters: FileContentFilter) {
        fileContentFilters.addAll(filters)
    }

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(content: PersistentSourceContent): DownloadTask {
        val downloadFiles = content.sourceFiles
            .filter { it.status != FileContentStatus.TARGET_EXISTS }
            .map { it.fileDownloadPath }
        if (log.isDebugEnabled) {
            log.debug("{} 创建下载任务文件, files:{}", content.sourceItem.title, downloadFiles)
        }
        return DownloadTask(content.sourceItem, downloadFiles, downloadPath, options.downloadOptions)
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
        return fileMover.rename(
            content.copy(sourceFiles = renameFiles)
        )
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
        private val retry = RetryTemplateBuilder()
            .maxAttempts(3)
            .fixedBackoff(Duration.ofSeconds(5L).toMillis())
            .withListener(LoggingRetryListener())
            .build()
    }

}

private val log = LoggerFactory.getLogger(SourceProcessor::class.java)

private class LoggingRetryListener : RetryListenerSupport() {

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>?,
        throwable: Throwable
    ) {
        val stage = context.getAttribute("stage")
        log.info(
            "第{}次重试失败, message:{}, stage:{}",
            context.retryCount,
            "${throwable::class.simpleName}:${throwable.message}",
            stage
        )
    }

    override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean {
        return super.open(context, callback)
    }

    override fun <T : Any?, E : Throwable?> close(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        super.close(context, callback, throwable)
    }
}

private class SimpleStat(
    private val name: String,
    var processingCounting: Int = 0,
    var filterCounting: Int = 0,
) {

    val stopWatch = StopWatch("$name:fetch")

    init {
        stopWatch.start()
    }

    fun incProcessingCounting() {
        processingCounting = processingCounting.inc()
    }

    fun incFilterCounting() {
        filterCounting = filterCounting.inc()
    }

    fun isChanged(): Boolean {
        return processingCounting > 0 || filterCounting > 0
    }

    override fun toString(): String {
        return "$name 处理了${processingCounting}个 过滤了${filterCounting}个, took:${stopWatch.totalTimeMillis}ms"
    }
}


private class SafeRunner(
    private val processor: SourceProcessor
) : Runnable {

    @Volatile
    private var running = false
    override fun run() {
        val name = processor.name
        log.info("Processor:${name} 触发获取源信息")
        if (running) {
            log.info("Processor:${name} 上一次任务还未完成，跳过本次任务")
            return
        }
        running = true
        try {
            processor.run()
        } catch (e: Exception) {
            log.error("Processor:${name} 执行失败", e)
        } finally {
            running = false
        }
    }
}

private class ProcessStage(
    val stage: String,
    val subject: Any?
) {
    override fun toString(): String {
        return "operation='$stage', subject=$subject"
    }
}

private class SourceHashingItemFilter(
    val sourceName: String,
    val processingStorage: ProcessingStorage
) : SourceItemFilter {
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