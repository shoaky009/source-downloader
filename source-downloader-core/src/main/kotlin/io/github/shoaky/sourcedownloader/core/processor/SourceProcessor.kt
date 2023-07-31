package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status.*
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
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
    private val source: Source<SourcePointer>,
    private val itemFileResolver: ItemFileResolver,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    sourceSavePath: Path,
    private val processingStorage: ProcessingStorage,
    private val options: ProcessorOptions = ProcessorOptions(),
) : Runnable {

    private val sourceItemFilters: List<SourceItemFilter> = options.sourceItemFilters
    private val itemContentFilters: List<ItemContentFilter> = options.itemContentFilters
    private val fileContentFilters: List<FileContentFilter> = options.fileContentFilters
    private val variableProviders = options.variableProviders
    private val runAfterCompletion: List<RunAfterCompletion> = options.runAfterCompletion
    private val taggers: List<FileTagger> = options.fileTaggers
    private var fileReplacementDecider: FileReplacementDecider = options.fileReplacementDecider
    private val downloadPath = downloader.defaultDownloadPath().toAbsolutePath()
    private val sourceSavePath: Path = sourceSavePath.toAbsolutePath()
    private val filenamePattern = options.filenamePattern
    private val savePathPattern = options.savePathPattern
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
            "ItemContentFilter" to itemContentFilters.map { it::class.simpleName },
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
            log.debug("Processor':${name}' 开始重命名任务...")
            var modified = false
            val measureTime = measureTime {
                try {
                    modified = runRename() > 0
                } catch (e: Exception) {
                    log.error("Processor:'${name}' 重命名任务出错", e)
                }
                System.currentTimeMillis()
            }

            if (modified) {
                log.info("Processor:'${name}' 重命名任务完成 took:${measureTime.inWholeMilliseconds}ms")
            }
            val renameCostTimeThreshold = 100L
            if (modified.not() && measureTime.inWholeMilliseconds > renameCostTimeThreshold) {
                log.warn("Processor:'${name}' 重命名任务没有修改 took:${measureTime.inWholeMilliseconds}ms")
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
            ?: ProcessorSourceState(
                processorName = name,
                sourceId = sourceId,
                lastPointer = Jackson.convert(source.defaultPointer(), PersistentPointer::class)
            )
        val sourcePointer = lastState.resolvePointer(source::class)
        log.debug("Processor:'{}' lastState:{}", name, lastState)

        val itemIterator = retry.execute<Iterator<PointedItem<ItemPointer>>, IOException> {
            it.setAttribute("stage", ProcessStage("FetchSourceItems", lastState))
            val iterator = source.fetch(sourcePointer, options.fetchLimit).iterator()
            iterator
        }

        val result = mutableListOf<ProcessingContent>()
        val simpleStat = SimpleStat(name)
        for (item in itemIterator) {
            val filterBy = sourceItemFilters.firstOrNull { it.test(item.sourceItem).not() }
            if (filterBy != null) {
                log.debug("{} Filtered item:{}", filterBy::class.simpleName, item)
                sourcePointer.update(item.pointer)
                if (options.pointerBatchMode.not() && dryRun.not()) {
                    saveState(sourcePointer, lastState)
                }
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
                log.error("Processor:'${name}'处理失败, item:$item", it)
                if (options.itemErrorContinue.not()) {
                    log.warn("Processor:'${name}'处理失败, item:$item, 退出本次触发处理, 如果未能解决该处理器将无法继续处理后续Item")
                    return result
                }
            }.onSuccess {
                if (dryRun) {
                    result.add(it)
                }
                sourcePointer.update(item.pointer)
                if (options.pointerBatchMode.not() && dryRun.not()) {
                    saveState(sourcePointer, lastState)
                }
            }.getOrElse {
                ProcessingContent(
                    name, CoreItemContent(
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

        if (dryRun.not() && options.pointerBatchMode) {
            saveState(sourcePointer, lastState)
        }
        return result
    }

    private fun saveState(sourcePointer: SourcePointer, lastState: ProcessorSourceState) {
        val sourceState = lastState.copy(
            lastPointer = Jackson.convert(sourcePointer, PersistentPointer::class),
            lastActiveTime = LocalDateTime.now()
        )

        if (sourceState.lastPointer != lastState.lastPointer) {
            log.info("Processor:'$name' update pointer:${sourceState.lastPointer}")
        }
        val save = processingStorage.save(sourceState)
        lastState.id = save.id
    }

    private fun processItem(sourceItem: SourceItem, dryRun: Boolean = false): ProcessingContent {
        val variablesAggregation = VariableProvidersAggregation(
            sourceItem,
            variableProviders.filter { it.support(sourceItem) }.toList(),
            options.variableConflictStrategy,
            options.variableNameReplace
        )
        val itemContent = createItemContent(variablesAggregation, sourceItem)
        val filterBy = itemContentFilters.firstOrNull { it.test(itemContent).not() }
        if (filterBy != null) {
            log.debug("{} Filtered item:{}", filterBy::class.simpleName, sourceItem)
            return ProcessingContent(name, itemContent).copy(status = FILTERED)
        }

        val replaceFiles = getReplaceableFiles(itemContent)
        val contentStatus = probeContent(itemContent, replaceFiles)

        if (dryRun) {
            return ProcessingContent(name, itemContent).copy(status = contentStatus.second)
        }

        if (contentStatus.first) {
            val downloadTask = createDownloadTask(itemContent, replaceFiles)
            // NOTE 非异步下载器会阻塞
            this.downloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${sourceItem.title}")
            val targetPaths = itemContent.getDownloadFiles(fileMover).map { it.targetPath() }
            saveTargetPaths(sourceItem, targetPaths)
            Events.post(ProcessorSubmitDownloadEvent(name, itemContent))
        }

        if (downloader !is AsyncDownloader) {
            val moveSuccess = moveFiles(itemContent)
            val replaceSuccess = replaceFiles(itemContent)
            if (moveSuccess || replaceSuccess) {
                runAfterCompletions(itemContent)
            }
            return ProcessingContent(name, itemContent).copy(status = RENAMED, renameTimes = 1)
        }
        var status = WAITING_TO_RENAME
        if (contentStatus.first.not()) {
            status = contentStatus.second
        }

        return ProcessingContent(name, itemContent).copy(status = status)
    }

    private fun getReplaceableFiles(itemContent: CoreItemContent): List<CoreFileContent> {
        itemContent.updateFileStatus(fileMover)
        if (fileReplacementDecider == NeverReplace) {
            return emptyList()
        }
        val partition = itemContent.sourceFiles.partition { it.status == FileContentStatus.REPLACE }
        val existsFiles = partition.second.filter { it.status == FileContentStatus.TARGET_EXISTS }
        val support = TargetPathRelationSupport(
            existsFiles,
            processingStorage
        )
        val dropMem = mutableMapOf<String, Boolean>()
        val replaceFiles = existsFiles
            .map { fileContent ->
                val copy = itemContent.copy(
                    sourceFiles = listOf(fileContent)
                )
                val before = support.getContent(fileContent.targetPath())
                checkBeforeProcessing(before, dropMem)
                val replace = fileReplacementDecider.isReplace(copy, before?.itemContent)
                fileContent to replace
            }.filter { it.second }
            .map { it.first }
            .onEach { it.status = FileContentStatus.REPLACE }
        return partition.first + replaceFiles
    }

    private fun checkBeforeProcessing(before: ProcessingContent?, dropMem: MutableMap<String, Boolean>) {
        if (before == null || before.status != WAITING_TO_RENAME) {
            return
        }
        val files = before.itemContent.sourceFiles.map {
            SourceFile(it.fileDownloadPath, it.attributes, it.fileUri)
        }
        // drop before task
        val beforeItem = before.itemContent.sourceItem
        val hashing = beforeItem.hashing()
        dropMem.computeIfAbsent(hashing) {
            log.info("Drop before id:{} task:{}", before.id, beforeItem)
            downloader.cancel(beforeItem, files)
            true
        }
    }

    private fun createItemContent(
        sourceItemGroup: SourceItemGroup,
        sourceItem: SourceItem
    ): CoreItemContent {
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
                    savePathPattern,
                    filenamePattern,
                    resolveFile.attributes,
                    fileUri = resolveFile.fileUri
                )
                val tags = taggers.mapNotNull { it.tag(sourceFileContent) }
                    .onEach {
                        sourceFileContent.tags.add(it)
                    }
                val fileOptions = options.getTaggedOptions(tags)
                val replacedContent = fileOptions.let { ops ->
                    sourceFileContent.copy(
                        filenamePattern = ops?.filenamePattern ?: filenamePattern,
                        fileSavePathPattern = ops?.savePathPattern ?: savePathPattern
                    )
                }
                replacedContent.setVariableErrorStrategy(options.variableErrorStrategy)
                replacedContent.addSharedVariables(sharedPatternVariables)
                replacedContent to fileOptions
            }.filter { pair ->
                val path = pair.first
                val filters = pair.second?.fileContentFilters ?: fileContentFilters
                val filter = filters.all { it.test(path) }
                if (filter.not()) {
                    log.debug("Filtered file:{}", path)
                }
                filter
            }.map { it.first }
        return CoreItemContent(sourceItem, sourceFiles, MapPatternVariables(sharedPatternVariables))
    }

    /**
     * @return Download or not, [ProcessingContent.Status]
     */
    private fun probeContent(sc: CoreItemContent, replaceFiles: List<CoreFileContent>): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        if (files.isEmpty()) {
            return false to NO_FILES
        }
        if (replaceFiles.isNotEmpty()) {
            return true to WAITING_TO_RENAME
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

    private fun runAfterCompletions(taskContent: ItemContent) {
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
            log.warn("Processor:'${name}' 非异步下载器不执行重命名任务")
            return 0
        }
        val downloadStatusGrouping = processingStorage.findRenameContent(name, options.renameTimesThreshold)
            .groupBy({ pc ->
                DownloadStatus.from(asyncDownloader.isFinished(pc.itemContent.sourceItem))
            }, { it })

        downloadStatusGrouping[DownloadStatus.NOT_FOUND]?.forEach { pc ->
            kotlin.runCatching {
                log.info("Processing下载任务不存在, record:${Jackson.toJsonString(pc)}")
                processingStorage.save(pc.copy(
                    status = DOWNLOAD_FAILED,
                    modifyTime = LocalDateTime.now(),
                ))
                processingStorage.deleteTargetPath(pc.itemContent.sourceFiles.map { it.targetPath() })
            }.onFailure {
                log.error("Processing更新状态出错, record:${Jackson.toJsonString(pc)}", it)
            }
        }

        downloadStatusGrouping[DownloadStatus.FINISHED]?.forEach { pc ->
            kotlin.runCatching {
                processRenameTask(pc)
            }.onFailure {
                log.error("Processing重命名任务出错, record:${Jackson.toJsonString(pc)}", it)
            }
        }
        return downloadStatusGrouping[DownloadStatus.FINISHED]?.size ?: 0
    }

    private fun processRenameTask(pc: ProcessingContent) {
        val itemContent = pc.itemContent
        val sourceFiles = itemContent.sourceFiles
        val replacers = options.variableReplacers.toTypedArray()
        sourceFiles.forEach {
            // 这边设计不太好, CoreFileContent改为纯数据, 计算targetPath改为RenameContext之类的来计算
            it.setVariableErrorStrategy(options.variableErrorStrategy)
            val filenamePattern = it.filenamePattern as CorePathPattern
            filenamePattern.addReplacer(*replacers)
            val fileSavePathPattern = it.fileSavePathPattern as CorePathPattern
            fileSavePathPattern.addReplacer(*replacers)
        }

        if (sourceFiles.all { it.status != FileContentStatus.REPLACE } &&
            fileMover.exists(sourceFiles.map { it.targetPath() })
        ) {
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
            moveFiles(itemContent)
            replaceFiles(itemContent)
        }.onFailure {
            log.error("重命名出错, record:${Jackson.toJsonString(pc)}", it)
        }.getOrDefault(false)

        if (allSuccess) {
            runAfterCompletions(itemContent)
        }
        val renameTimesThreshold = options.renameTimesThreshold
        if (pc.renameTimes == renameTimesThreshold) {
            log.warn("重命名${renameTimesThreshold}次重试失败, record:${Jackson.toJsonString(pc)}")
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
            paths.map { ProcessingTargetPath(it) }
        } else {
            paths.map { ProcessingTargetPath(it, name, sourceItem.hashing()) }
        }
        processingStorage.saveTargetPath(processingTargetPaths)
    }

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(content: CoreItemContent, replaceFiles: List<CoreFileContent>): DownloadTask {
        val downloadFiles = content.sourceFiles
            .filter { it.status != FileContentStatus.TARGET_EXISTS }
            .toMutableList()
        if (log.isDebugEnabled) {
            log.debug("{} 创建下载任务文件, files:{}", content.sourceItem.title, downloadFiles)
        }

        downloadFiles.addAll(replaceFiles)
        val headers = HashMap(source.headers())
        headers.putAll(options.downloadOptions.headers)
        val downloadOptions = options.downloadOptions
        return DownloadTask(
            content.sourceItem,
            downloadFiles.map {
                SourceFile(it.fileDownloadPath, it.attributes, it.fileUri)
            },
            downloadPath,
            downloadOptions.copy(headers = headers)
        )
    }

    private fun moveFiles(content: CoreItemContent): Boolean {
        val movableFiles = content.getMovableFiles(fileMover)
        if (movableFiles.isEmpty()) {
            log.info("Processor:'$name' item:'${content.sourceItem.title}' no available files to rename")
            return false
        }
        movableFiles.map { it.saveDirectoryPath() }
            .distinct()
            .forEach {
                fileMover.createDirectories(it)
            }

        if (log.isDebugEnabled) {
            content.sourceFiles.forEach {
                log.debug("Move file:'${it.fileDownloadPath}' to '${it.targetPath()}'")
            }
        }
        return fileMover.move(
            content.copy(sourceFiles = movableFiles)
        )
    }

    private fun replaceFiles(itemContent: CoreItemContent): Boolean {
        val replaceableFiles = getReplaceableFiles(itemContent)
        if (replaceableFiles.isEmpty()) {
            return true
        }
        log.info(
            "Processor:'{}' sourceItem:{} replaceFiles:{}",
            name, itemContent.sourceItem.title, replaceableFiles.map { it.targetPath() }
        )

        return fileMover.replace(itemContent.copy(sourceFiles = replaceableFiles))
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
        log.warn(
            "第{}次重试失败, {}, message:{}",
            context.retryCount,
            stage,
            "${throwable::class.simpleName}:${throwable.message}",
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
        log.info("Processor:'${name}' 触发获取源信息")
        if (running) {
            log.info("Processor:'${name}' 上一次任务还未完成，跳过本次任务")
            return
        }
        running = true
        try {
            processor.run()
        } catch (e: Exception) {
            log.error("Processor:'${name}' 执行失败", e)
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
        return "stage:'$stage', subject:$subject"
    }
}

private class TargetPathRelationSupport(
    files: List<CoreFileContent>,
    processingStorage: ProcessingStorage
) {

    private val targetPaths = processingStorage.findTargetPaths(
        files.map { it.targetPath() }
    )
    private val contents = processingStorage.findByItemHashing(
        targetPaths.mapNotNull { it.itemHashing }.distinct()
    ).filter { it.status == RENAMED }
        .groupBy { it.sourceHash }
        .mapValues { ent -> ent.value.maxBy { it.createTime } }
    private val targetPathMapping = targetPaths.filter { it.itemHashing != null }
        .associateBy { it.targetPath }

    fun getContent(targetPath: Path): ProcessingContent? {
        val processingTargetPath = targetPathMapping[targetPath] ?: return null
        val itemHashing = processingTargetPath.itemHashing ?: return null
        return contents[itemHashing]
    }
}
