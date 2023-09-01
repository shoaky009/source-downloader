package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.core.PersistentPointer
import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status.*
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.file.*
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.util.LoggingStageRetryListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.util.StopWatch
import java.io.IOException
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName
import kotlin.time.measureTime

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
) : Runnable, AutoCloseable {

    private val directDownloader = DirectDownloader(downloader)
    private val downloadPath = downloader.defaultDownloadPath().toAbsolutePath()
    private val sourceSavePath: Path = sourceSavePath.toAbsolutePath()
    private val filenamePattern = options.filenamePattern as CorePathPattern
    private val savePathPattern = options.savePathPattern as CorePathPattern
    private var renameTaskFuture: ScheduledFuture<*>? = null
    private val sourceItemFilters: List<SourceItemFilter> = options.sourceItemFilters
    private val itemContentFilters: List<ItemContentFilter> = options.itemContentFilters
    private val fileContentFilters: List<FileContentFilter> = options.fileContentFilters
    private val variableProviders = options.variableProviders
    private val runAfterCompletion: List<RunAfterCompletion> = options.runAfterCompletion
    private val taggers: List<FileTagger> = options.fileTaggers
    private val fileReplacementDecider: FileReplacementDecider = options.fileReplacementDecider
    private val fileExistsDetector: FileExistsDetector = options.fileExistsDetector
    private val auxiliaryFileMover = IncludingTargetPathsFileMover(
        ReadonlyFileMover(fileMover),
        processingStorage,
    )

    private val renamer: Renamer = Renamer(
        options.variableErrorStrategy,
        options.variableReplacers,
    )
    private val safeRunner by lazy {
        ProcessorSafeRunner(this)
    }
    private val renameScheduledExecutor by lazy {
        Executors.newSingleThreadScheduledExecutor()
    }
    private val itemChannel = Channel<Process>(options.channelBufferSize)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        scheduleRenameTask(options.renameTaskInterval)
        listenChannel()
    }

    private fun listenChannel() {
        coroutineScope.launch {
            for (process in itemChannel) {
                process.run()
            }
        }
    }

    override fun run() {
        NormalProcess().run()
    }

    fun dryRun(): List<ProcessingContent> {
        val process = DryRunProcess()
        process.run()
        return process.getResult()
    }

    suspend fun run(sourceItems: List<SourceItem>) {
        // ManualItemProcess(sourceItems).run()
        itemChannel.send(ManualItemProcess(sourceItems))
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

    private fun identifyFilesToReplace(itemContent: CoreItemContent): List<CoreFileContent> {
        if (fileReplacementDecider == NeverReplace) {
            return emptyList()
        }

        val existsFiles = itemContent.sourceFiles.filter { it.status == FileContentStatus.TARGET_EXISTS }
        val support = TargetPathRelationSupport(
            itemContent.sourceItem,
            existsFiles,
            processingStorage
        )
        val discardedItems = mutableMapOf<String, Boolean>()
        val replaceFiles = existsFiles
            .map { fileContent ->
                val before = support.getBeforeContent(fileContent.targetPath())
                cancelBeforeProcessing(before, fileContent, discardedItems)
                val copy = itemContent.copy(
                    sourceFiles = listOf(fileContent)
                )
                val replace = fileReplacementDecider.isReplace(copy, before?.itemContent)
                fileContent to replace
            }.filter { it.second }
            .map { it.first }
            .onEach {
                it.status = FileContentStatus.READY_REPLACE
            }
        return replaceFiles
    }

    private fun cancelBeforeProcessing(
        before: ProcessingContent?,
        existsFile: CoreFileContent,
        discardedItems: MutableMap<String, Boolean>) {
        if (before == null || before.status != WAITING_TO_RENAME) {
            return
        }
        val files = existsFile.let {
            SourceFile(it.fileDownloadPath, it.attrs, it.fileUri)
        }
        // drop before task
        val beforeItem = before.itemContent.sourceItem
        val hashing = beforeItem.hashing()
        discardedItems.computeIfAbsent(hashing) {
            log.info("Processor:'{}' drop before id:{} task:{}", name, before.id, beforeItem)
            // cancel整个item不太合理，需要以文件的纬度按需取消
            downloader.cancel(beforeItem, listOf(files))
            true
        }
    }

    private fun createItemContent(sourceItemGroup: SourceItemGroup, sourceItem: SourceItem): CoreItemContent {
        val sharedPatternVariables = sourceItemGroup.sharedPatternVariables()
        val resolvedFiles = itemFileResolver.resolveFiles(sourceItem).map { file ->
            val tags = taggers.mapNotNull { it.tag(file) }.toMutableSet()
            if (tags.isEmpty() && file.tags.isEmpty()) {
                return@map file
            }
            tags.addAll(file.tags)
            file.copy(tags = tags)
        }
        checkResolvedFiles(sourceItem, resolvedFiles)

        val fileContents = resolvedFiles.groupBy {
            options.matchFileOption(it)
        }.flatMap { entry ->
            val fileOption = entry.key
            val files = entry.value
            val fileVariables = sourceItemGroup.filePatternVariables(files)
            files.mapIndexed { index, file ->
                val rawFileContent = RawFileContent(
                    sourceSavePath,
                    downloadPath,
                    MapPatternVariables(fileVariables[index].patternVariables()),
                    fileOption?.savePathPattern ?: savePathPattern,
                    fileOption?.filenamePattern ?: filenamePattern,
                    file
                )
                val fileContent = renamer.createFileContent(sourceItem, rawFileContent, sharedPatternVariables)
                fileContent to fileOption
            }
        }.filter { pair ->
            val path = pair.first
            val filters = pair.second?.fileContentFilters ?: fileContentFilters
            val filter = filters.all { it.test(path) }
            if (filter.not()) {
                log.debug("Filtered file:{}", path)
            }
            filter
        }.map { it.first }
        return CoreItemContent(sourceItem, fileContents, MapPatternVariables(sharedPatternVariables))
    }

    private fun checkResolvedFiles(sourceItem: SourceItem, resolvedFiles: List<SourceFile>) {
        val duplicated = resolvedFiles.groupBy { it.path }
            .filter { it.value.size > 1 }
            .map { it.key }
        if (duplicated.isNotEmpty()) {
            log.error("Processor:'{}' resolver:{} resolved item:{} duplicated files:{}, It's likely that there's an issue with the component's implementation.",
                name, itemFileResolver::class.jvmName, sourceItem, duplicated)
            throw IllegalStateException("Duplicated files:$duplicated")
        }
    }

    /**
     * @return Download or not, [ProcessingContent.Status]
     */
    private fun probeContentStatus(sc: CoreItemContent, replaceFiles: List<CoreFileContent>): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        if (files.isEmpty()) {
            return false to NO_FILES
        }
        if (replaceFiles.isNotEmpty()) {
            return true to WAITING_TO_RENAME
        }

        // 预防这一批次的Item有相同的目标，并且是AsyncDownloader的情况下会重复下载
        if (files.all { it.status == FileContentStatus.TARGET_EXISTS }) {
            log.info("Item:{} already exists, files:{}", sc.sourceItem, sc.sourceFiles)
            return false to TARGET_ALREADY_EXISTS
        }
        val allExists = fileMover.exists(files.map { it.fileDownloadPath }).all { it }
        return if (allExists) {
            false to WAITING_TO_RENAME
        } else {
            true to WAITING_TO_RENAME
        }
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

        if (sourceFiles.all { it.status != FileContentStatus.READY_REPLACE } &&
            // 是否有必要判断实时的?
            fileMover.exists(sourceFiles.map { it.targetPath() }).all { it }
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

        itemContent.updateFileStatus(fileMover, fileExistsDetector)
        val allSuccess = runCatching {
            val moved = moveFiles(itemContent)
            val replaced = replaceFiles(itemContent)
            moved || replaced
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
        processingStorage.saveTargetPaths(processingTargetPaths)
    }

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(content: CoreItemContent, replaceFiles: List<CoreFileContent>): DownloadTask {
        val downloadFiles = content.sourceFiles
            .filter { it.status != FileContentStatus.TARGET_EXISTS }
            .toMutableList()
            .apply { this.addAll(replaceFiles) }
            .distinct()
        if (log.isDebugEnabled) {
            log.debug("{} 创建下载任务文件, files:{}", content.sourceItem.title, downloadFiles)
        }

        val headers = HashMap(source.headers())
        headers.putAll(options.downloadOptions.headers)
        val downloadOptions = options.downloadOptions
        return DownloadTask(
            content.sourceItem,
            downloadFiles.map {
                SourceFile(it.fileDownloadPath, it.attrs, it.fileUri)
            },
            downloadPath,
            downloadOptions.copy(headers = headers)
        )
    }

    private fun moveFiles(content: CoreItemContent): Boolean {
        val movableFiles = content.movableFiles()
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
        val replaceableFiles =
            itemContent.sourceFiles.filter { it.status == FileContentStatus.READY_REPLACE }

        if (replaceableFiles.isEmpty()) {
            return true
        }
        log.info(
            "Processor:'{}' sourceItem:{} replaceFiles:{}",
            name, itemContent.sourceItem.title, replaceableFiles.map { it.targetPath() }
        )

        val replace = fileMover.replace(itemContent.copy(sourceFiles = replaceableFiles))
        if (replace) {
            itemContent.sourceFiles.filter { it.status == FileContentStatus.READY_REPLACE }
                .onEach { it.status = FileContentStatus.REPLACE }
        }
        return replace
    }

    override fun toString(): String {
        return info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n")
    }

    override fun close() {
        renameTaskFuture?.cancel(false)
        renameScheduledExecutor.shutdown()
        coroutineScope.cancel("Processor:${name} closed")
        itemChannel.close()
    }

    companion object {

        private val retry = RetryTemplateBuilder()
            .maxAttempts(3)
            .fixedBackoff(Duration.ofSeconds(5L).toMillis())
            .withListener(LoggingStageRetryListener())
            .build()

        private val filteredStatuses = setOf(FILTERED, TARGET_ALREADY_EXISTS)
    }

    private abstract inner class Process(
        protected val lastState: ProcessorSourceState = processingStorage.findProcessorSourceState(name, sourceId)
            ?: ProcessorSourceState(
                processorName = name,
                sourceId = sourceId,
                lastPointer = Jackson.convert(source.defaultPointer(), PersistentPointer::class)
            ),
        protected val sourcePointer: SourcePointer = lastState.resolvePointer(source::class),
        private val itemIterable: Iterable<PointedItem<ItemPointer>> = retry.execute<Iterable<PointedItem<ItemPointer>>, IOException> {
            it.setAttribute("stage", ProcessStage("FetchSourceItems", lastState))
            source.fetch(sourcePointer, options.fetchLimit)
        }
    ) {

        protected fun processItems() {
            retry.execute<Iterator<PointedItem<ItemPointer>>, IOException> {
                it.setAttribute("stage", ProcessStage("FetchSourceItems", lastState))
                val iterator = source.fetch(sourcePointer, options.fetchLimit).iterator()
                iterator
            }

            val simpleStat = SimpleStat(name)
            for (item in itemIterable) {
                log.trace("Processor:'{}' start process item:{}", name, item.sourceItem)

                val filterBy = sourceItemFilters.firstOrNull { it.test(item.sourceItem).not() }
                if (filterBy != null) {
                    log.info("{} filtered item:{}", filterBy::class.simpleName, item.sourceItem)
                    onItemFiltered(item)
                    simpleStat.incFilterCounting()
                    continue
                }
                val processingContent = kotlin.runCatching {
                    retry.execute<ProcessingContent, IOException> {
                        it.setAttribute("stage", ProcessStage("ProcessItem", item))
                        processItem(item.sourceItem)
                    }
                }.onFailure {
                    log.error("Processor:'${name}'处理失败, item:$item", it)
                    if (options.itemErrorContinue.not()) {
                        log.warn("Processor:'${name}'处理失败, item:$item, 退出本次触发处理, 如果未能解决该处理器将无法继续处理后续Item")
                        return
                    }
                }.onSuccess {
                    onItemSuccess(item, it)
                }.getOrElse {
                    ProcessingContent(
                        name, CoreItemContent(
                        item.sourceItem, emptyList(), MapPatternVariables()
                    )).copy(status = FAILURE, failureReason = it.message)
                }

                log.trace("Processor:'{}' finished process item:{}", name, item.sourceItem)
                onItemCompletion(processingContent)

                if (filteredStatuses.contains(processingContent.status)) {
                    simpleStat.incFilterCounting()
                } else {
                    simpleStat.incProcessingCounting()
                }
            }
            onCompletion()

            simpleStat.stopWatch.stop()
            if (simpleStat.isChanged()) {
                log.info("Processor:{}", simpleStat)
            }
        }

        private fun processItem(sourceItem: SourceItem): ProcessingContent {
            val variablesAggregation = VariableProvidersAggregation(
                sourceItem,
                variableProviders.filter { it.support(sourceItem) }.toList(),
                options.variableConflictStrategy,
                options.variableNameReplace
            )
            val itemContent = createItemContent(variablesAggregation, sourceItem)
            log.trace("Processor:'{}' created content item:{}", name, sourceItem)

            val filterBy = itemContentFilters.firstOrNull { it.test(itemContent).not() }
            if (filterBy != null) {
                log.info("{} filtered item:{}", filterBy::class.simpleName, sourceItem)
                return ProcessingContent(name, itemContent).copy(status = FILTERED)
            }

            // 后面再调整状态的更新方式
            itemContent.updateFileStatus(auxiliaryFileMover, fileExistsDetector)
            val processingTargetPaths = processingStorage.targetPathExists(
                itemContent.sourceFiles.map { it.targetPath() },
                itemContent.sourceItem.hashing()
            )
            itemContent.sourceFiles.onEachIndexed { index, fileContent ->
                if (fileContent.status == FileContentStatus.NORMAL && processingTargetPaths[index]) {
                    fileContent.status = FileContentStatus.TARGET_EXISTS
                }
            }

            val replaceFiles = identifyFilesToReplace(itemContent)
            val (shouldDownload, contentStatus) = probeContentStatus(itemContent, replaceFiles)
            log.trace("Processor:'{}' item:{} ,should download: {}, content status:{}",
                name, sourceItem, shouldDownload, contentStatus)
            val processingContent = ProcessingContent(name, itemContent).copy(status = contentStatus)

            if (shouldDownload) {
                log.trace("Processor:'{}' start download item:{}", name, sourceItem)
                val success = doDownload(processingContent, replaceFiles)
                if (success && downloader !is AsyncDownloader) {
                    log.trace("Processor:'{}' start rename item:{}", name, sourceItem)
                    val moveSuccess = moveFiles(itemContent)
                    val replaceSuccess = replaceFiles(itemContent)
                    if (moveSuccess || replaceSuccess) {
                        runAfterCompletions(itemContent)
                    }
                    return ProcessingContent(name, itemContent).copy(status = RENAMED, renameTimes = 1)
                }
            }

            return processingContent
        }

        open fun onCompletion() {}

        open fun onItemCompletion(processingContent: ProcessingContent) {}

        open fun onItemSuccess(item: PointedItem<ItemPointer>, processingContent: ProcessingContent) {}

        open fun onItemFiltered(item: PointedItem<ItemPointer>) {}

        open fun doDownload(pc: ProcessingContent, replaceFiles: List<CoreFileContent>): Boolean {
            val itemContent = pc.itemContent
            val downloadTask = createDownloadTask(itemContent, replaceFiles)
            // NOTE 非异步下载器会阻塞
            directDownloader.submit(downloadTask)
            log.info("提交下载任务成功, Processor:${name} sourceItem:${itemContent.sourceItem.title}")
            val targetPaths = itemContent.downloadableFiles().map { it.targetPath() }
            // TODO 应该先ProcessingContent后再保存targetPaths
            saveTargetPaths(itemContent.sourceItem, targetPaths)
            // Events.post(ProcessorSubmitDownloadEvent(name, itemContent))
            return true
        }

        fun run() {
            processItems()
        }
    }

    private inner class NormalProcess : Process() {

        override fun onCompletion() {
            if (options.pointerBatchMode) {
                saveSourceState()
            }
        }

        override fun onItemCompletion(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                processingStorage.save(processingContent)
            }
        }

        override fun onItemSuccess(item: PointedItem<ItemPointer>, processingContent: ProcessingContent) {
            sourcePointer.update(item.pointer)
            if (options.pointerBatchMode.not()) {
                saveSourceState()
            }
        }

        override fun onItemFiltered(item: PointedItem<ItemPointer>) {
            sourcePointer.update(item.pointer)
            if (options.pointerBatchMode.not()) {
                saveSourceState()
            }
        }

        private fun saveSourceState() {
            val currentSourceState = lastState.copy(
                lastPointer = Jackson.convert(sourcePointer, PersistentPointer::class),
                lastActiveTime = LocalDateTime.now()
            )
            val lastP = lastState.resolvePointer(source::class)
            val currP = currentSourceState.resolvePointer(source::class)
            if (currP != lastP) {
                log.info("Processor:'$name' update pointer:${currentSourceState.lastPointer}")
            }
            val save = processingStorage.save(currentSourceState)
            lastState.id = save.id
        }
    }

    private inner class DryRunProcess : Process() {

        private val result: MutableList<ProcessingContent> = mutableListOf()

        override fun onItemCompletion(processingContent: ProcessingContent) {
            result.add(processingContent)
        }

        fun getResult(): List<ProcessingContent> {
            return result
        }

        override fun doDownload(pc: ProcessingContent, replaceFiles: List<CoreFileContent>): Boolean = false
    }

    private inner class ManualItemProcess(items: List<SourceItem>) : Process(
        itemIterable = items.map {
            PointedItem(it, NullPointer)
        }) {

        override fun onItemCompletion(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                processingStorage.save(processingContent)
            }
        }
    }
}

val log: Logger = LoggerFactory.getLogger(SourceProcessor::class.java)

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
        return "'$name' 处理了${processingCounting}个 过滤了${filterCounting}个, took:${stopWatch.totalTimeMillis}ms"
    }
}