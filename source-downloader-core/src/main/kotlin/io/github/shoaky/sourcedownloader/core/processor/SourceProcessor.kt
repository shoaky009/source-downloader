package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status.*
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.CoreSourceContent
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.util.Events
import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.util.StopWatch
import java.io.IOException
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.time.measureTime

// TODO 基于行为重构，async,sync,dry-run,...
/**
 * 拉在这里，后面看情况重构
 */
class SourceProcessor(
    val name: String,
    // 先这样传入 后面要改
    private val sourceId: String,
    private val source: Source<SourceItemPointer>,
    private val itemFileResolver: ItemFileResolver,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    sourceSavePath: Path,
    private val processingStorage: ProcessingStorage,
    private val options: ProcessorOptions = ProcessorOptions(),
) : Runnable {

    private val sourceItemFilters: List<SourceItemFilter> = options.sourceItemFilters
    private val sourceContentFilters: List<SourceContentFilter> = options.sourceContentFilters
    private val fileContentFilters: List<FileContentFilter> = options.fileContentFilters
    private val variableProviders = options.variableProviders
    private val runAfterCompletion: List<RunAfterCompletion> = options.runAfterCompletion
    private val taggers: List<FileTagger> = options.fileTaggers
    private var fileReplacementDecider: FileReplacementDecider = options.fileReplacementDecider

    private val downloadPath = downloader.defaultDownloadPath().toAbsolutePath()
    private val sourceSavePath: Path = sourceSavePath.toAbsolutePath()

    private val variableReplacers: MutableList<VariableReplacer> = mutableListOf(
        *options.variableReplacers.toTypedArray(), WindowsPathReplacer
    )

    private val fileSavePathPattern: PathPattern = CorePathPattern(
        options.savePathPattern.pattern,
        variableReplacers
    )

    private val filenamePattern: PathPattern = CorePathPattern(
        options.filenamePattern.pattern,
        variableReplacers
    )

    private val tagFilenamePattern = options.taggedFileOptions.mapValues {
        it.value.filenamePattern?.let { pattern ->
            CorePathPattern(pattern.pattern, variableReplacers)
        }
    }

    private var renameTaskFuture: ScheduledFuture<*>? = null
    private val safeRunner by lazy {
        SafeRunner(this)
    }
    private val renameScheduledExecutor by lazy {
        Executors.newSingleThreadScheduledExecutor()
    }

    init {
        scheduleRenameTask(options.renameTaskInterval)
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
            "SourceContentFilter" to sourceContentFilters.map { it::class.simpleName },
            "RunAfterCompletion" to runAfterCompletion.map { it::class.simpleName },
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "FileContentFilter" to fileContentFilters.map { it::class.simpleName },
            "Taggers" to taggers.map { it::class.simpleName },
            "Options" to options,
        )
    }

    private fun scheduleRenameTask(interval: Duration) {
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
        var lastState = processingStorage.findProcessorSourceState(name, sourceId)
        log.debug("Processor:{} lastState:{}", name, lastState)

        val itemIterator = retry.execute<Iterator<PointedItem<SourceItemPointer>>, IOException> {
            it.setAttribute("stage", ProcessStage("FetchSourceItems", lastState))
            val pointer = lastState?.resolvePointer(source::class)
            val iterator = source.fetch(pointer, options.fetchLimit).iterator()
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
                retry.execute<ProcessingContent, IOException> {
                    it.setAttribute("stage", ProcessStage("ProcessItem", item))
                    processItem(item.sourceItem, dryRun)
                }
            }.onFailure {
                // 失败的hook
                log.error("Processor:${name}处理失败, item:$item", it)
                if (options.itemErrorContinue.not()) {
                    log.warn("Processor:${name}处理失败, item:$item, 退出本次触发处理, 如果未能解决该处理器将无法继续处理后续Item")
                    return result
                }
            }.onSuccess {
                if (dryRun) {
                    result.add(it)
                }
                // 也许有一直失败的会卡住整体，暂时先这样处理后面用retryTimes来判断
                lastPointedItem = item
                if (options.pointerBatchMode.not() && dryRun.not()) {
                    saveSourceState(lastPointedItem, lastState)
                    lastState = processingStorage.findProcessorSourceState(name, sourceId)
                }
            }.getOrElse {
                ProcessingContent(
                    name, CoreSourceContent(
                    item.sourceItem, emptyList(), MapPatternVariables()
                )).copy(status = FAILURE, failureReason = it.message)
            }

            if (options.saveProcessingContent && dryRun.not()) {
                processingStorage.save(processingContent)
            }

            if (FILTERED == processingContent.status || TARGET_ALREADY_EXISTS == processingContent.status) {
                simpleStat.incFilterCounting()
            } else {
                simpleStat.incProcessingCounting()
            }
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
        val filterBy = sourceContentFilters.firstOrNull { it.test(sourceContent).not() }
        if (filterBy != null) {
            log.debug("{} Filtered item:{}", filterBy::class.simpleName, sourceItem)
            return ProcessingContent(name, sourceContent).copy(status = FILTERED)
        }

        val contentStatus = probeContent(sourceContent)
        val replaceFiles = getReplaceableFiles(sourceContent)

        if (contentStatus.first && dryRun.not()) {
            val downloadTask = createDownloadTask(sourceContent, replaceFiles)
            // NOTE 非异步下载会阻塞
            this.downloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${sourceItem.title}")
            val targetPaths = sourceContent.getDownloadFiles(fileMover).map { it.targetPath() }
            saveTargetPaths(sourceItem, targetPaths)
            Events.post(ProcessorSubmitDownloadEvent(name, sourceContent))
        }

        if (downloader !is AsyncDownloader && dryRun.not()) {
            val moveSuccess = moveFiles(sourceContent)
            val replaceSuccess = replaceFiles(sourceContent)
            if (moveSuccess || replaceSuccess) {
                runAfterCompletions(sourceContent)
            }
            return ProcessingContent(name, sourceContent).copy(status = RENAMED, renameTimes = 1)
        }

        var status = WAITING_TO_RENAME
        if (contentStatus.first.not() && downloader is AsyncDownloader) {
            status = contentStatus.second
        }

        return ProcessingContent(name, sourceContent).copy(status = status)
    }

    private fun getReplaceableFiles(sourceContent: CoreSourceContent): List<CoreFileContent> {
        sourceContent.updateFileStatus(fileMover)
        if (fileReplacementDecider == NeverReplace) {
            return emptyList()
        }

        val replaceFiles = sourceContent.sourceFiles
            .filter { it.status == FileContentStatus.TARGET_EXISTS }
            .map { cfc ->
                val copy = sourceContent.copy(
                    sourceFiles = listOf(cfc)
                )

                val before = findBeforeContent(cfc)
                val replace = fileReplacementDecider.isReplace(copy, before)
                cfc to replace
            }.filter { it.second }
            .map { it.first }
        return replaceFiles
    }

    private fun findBeforeContent(fileContent: CoreFileContent): CoreSourceContent? {
        val before = processingStorage.findTargetPath(fileContent.targetPath())?.let {
            return@let if (it.processorName != null && it.itemHashing != null) {
                processingStorage.findByNameAndHash(it.processorName, it.itemHashing)?.sourceContent
            } else {
                null
            }
        }?.let { pc ->
            val filter = pc.sourceFiles.filter { it.targetPath() == fileContent.targetPath() }
            pc.copy(sourceFiles = filter)
        }
        return before
    }

    private fun createPersistentSourceContent(
        sourceItemGroup: SourceItemGroup,
        sourceItem: SourceItem
    ): CoreSourceContent {
        val sharedPatternVariables = sourceItemGroup.sharedPatternVariables()
        val resolveFiles = itemFileResolver.resolveFiles(sourceItem)
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
                val tags = taggers.mapNotNull { it.tag(sourceFileContent) }
                sourceFileContent.tags.addAll(tags)

                val tagged = replaceFilenamePattern(sourceFileContent)
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
        return CoreSourceContent(sourceItem, sourceFiles, MapPatternVariables(sharedPatternVariables))
    }

    private fun replaceFilenamePattern(fileContent: CoreFileContent): CoreFileContent {
        val customTags = tagFilenamePattern.keys
        if (customTags.isEmpty()) {
            return fileContent
        }
        val tagged = customTags.filter {
            fileContent.isTagged(it)
        }
        log.debug("Processor:{} 文件:{} 标签:{}", name, fileContent.fileDownloadPath, tagged)
        val pathPatterns = tagged.mapNotNull {
            tagFilenamePattern[it]
        }
        if (pathPatterns.isEmpty()) {
            return fileContent
        }
        val taggedFilePattern = pathPatterns.first()
        log.debug("Processor:{} 文件:{} 使用自定义命名规则:{}", name, fileContent.fileDownloadPath, taggedFilePattern)
        return fileContent.copy(filenamePattern = taggedFilePattern)
    }

    /**
     * @return Download or not, [ProcessingContent.Status]
     */
    private fun probeContent(sc: CoreSourceContent): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        if (files.isEmpty()) {
            return false to NO_FILES
        }

        val targetPaths = files.map { it.targetPath() }
        // 预防这一批次的Item有相同的目标，并且是AsyncDownloader的情况下会重复下载
        if (processingStorage.targetPathExists(targetPaths)) {
            return false to TARGET_ALREADY_EXISTS
        }

        if (fileMover.exists(targetPaths)) {
            return false to TARGET_ALREADY_EXISTS
        }

        val current = files.map { it.fileDownloadPath.exists() }
        if (current.all { it }) {
            return false to WAITING_TO_RENAME
        }
        val any = current.any { it.not() }
        if (any) {
            return true to WAITING_TO_RENAME
        }
        return false to DOWNLOADED
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
            log.warn("Processor:${name} 非异步下载器不执行重命名任务")
            return 0
        }
        val contentGrouping = processingStorage.findRenameContent(name, options.renameTimesThreshold)
            .groupBy(
                { pc ->
                    DownloadStatus.from(asyncDownloader.isFinished(pc.sourceContent.sourceItem))
                }, { it }
            )

        contentGrouping[DownloadStatus.NOT_FOUND]?.forEach { pc ->
            kotlin.runCatching {
                log.info("Processing下载任务不存在, record:${Jackson.toJsonString(pc)}")
                processingStorage.save(
                    pc.copy(
                        status = DOWNLOAD_FAILED,
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
            val filenamePattern = it.filenamePattern as CorePathPattern
            filenamePattern.addReplacer(*replacers)
            val fileSavePathPattern = it.fileSavePathPattern as CorePathPattern
            fileSavePathPattern.addReplacer(*replacers)
        }

        val targetPaths = sourceFiles.map { it.targetPath() }
        if (fileMover.exists(targetPaths)) {
            val toUpdate = pc.copy(
                renameTimes = pc.renameTimes.inc(),
                status = TARGET_ALREADY_EXISTS,
                modifyTime = LocalDateTime.now(),
            )
            processingStorage.save(toUpdate)
            log.info("全部目标文件已存在，无需重命名，record:${Jackson.toJsonString(pc)}")
            return
        }

        val allSuccess = runCatching {
            moveFiles(sourceContent)
            replaceFiles(sourceContent)
        }.onFailure {
            log.error("重命名出错, record:${Jackson.toJsonString(pc)}", it)
        }.getOrDefault(false)

        if (allSuccess) {
            runAfterCompletions(sourceContent)
        }

        val renameTimesThreshold = options.renameTimesThreshold
        if (pc.renameTimes == renameTimesThreshold) {
            log.warn("重命名${renameTimesThreshold}次重试失败record:${Jackson.toJsonString(pc)}")
        }

        val toUpdate = pc.copy(
            renameTimes = pc.renameTimes.inc(),
            status = RENAMED,
            modifyTime = LocalDateTime.now()
        )
        processingStorage.save(toUpdate)
    }

    private fun saveTargetPaths(sourceItem: SourceItem, paths: List<Path>) {
        val processingTargetPaths = if (fileReplacementDecider == NeverReplace) {
            ProcessingTargetPath(paths)
        } else {
            ProcessingTargetPath(paths, name, sourceItem.hashing())
        }
        processingStorage.saveTargetPath(processingTargetPaths)
    }

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(content: CoreSourceContent, replaceFiles: List<CoreFileContent>): DownloadTask {
        val downloadFiles = content.sourceFiles
            .filter { it.status != FileContentStatus.TARGET_EXISTS }
            .map { it.fileDownloadPath }.toMutableList()
        if (log.isDebugEnabled) {
            log.debug("{} 创建下载任务文件, files:{}", content.sourceItem.title, downloadFiles)
        }

        downloadFiles.addAll(replaceFiles.map { it.fileDownloadPath })
        return DownloadTask(
            content.sourceItem,
            downloadFiles,
            downloadPath,
            options.downloadOptions
        )
    }

    private fun moveFiles(content: CoreSourceContent): Boolean {
        val movableFiles = content.getMovableFiles(fileMover)
        if (movableFiles.isEmpty()) {
            log.info("Processor:$name item:'${content.sourceItem.title}' no available files to rename")
            return true
        }
        movableFiles.map { it.saveDirectoryPath() }
            .distinct()
            .forEach {
                fileMover.createDirectories(it)
            }
        return fileMover.move(
            content.copy(sourceFiles = movableFiles)
        )
    }

    private fun replaceFiles(sourceContent: CoreSourceContent): Boolean {
        val replaceableFiles = getReplaceableFiles(sourceContent)
        if (replaceableFiles.isEmpty()) {
            return true
        }
        log.info(
            "Processor:{} sourceItem:{} replaceFiles:{}",
            name, sourceContent.sourceItem.title, replaceableFiles.map { it.targetPath() }
        )

        return fileMover.replace(sourceContent.copy(sourceFiles = replaceableFiles))
    }

    override fun toString(): String {
        return info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n")
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

private class LoggingRetryListener : RetryListener {

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
        return "stage='$stage', subject=$subject"
    }
}

class SourceHashingItemFilter(
    private val sourceName: String,
    private val processingStorage: ProcessingStorage
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