package io.github.shoaky.sourcedownloader.core.processor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.github.michaelbull.retry.policy.*
import com.github.michaelbull.retry.retry
import io.github.shoaky.sourcedownloader.component.NeverReplace
import io.github.shoaky.sourcedownloader.component.downloader.NoneDownloader
import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.core.PersistentPointer
import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status.*
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.component.SourceHashingItemFilter
import io.github.shoaky.sourcedownloader.core.file.*
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.util.JsonComparator
import io.github.shoaky.sourcedownloader.util.NoLock
import io.github.shoaky.sourcedownloader.util.lock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.reflect.jvm.jvmName
import kotlin.time.measureTime

/**
 * 拉在这里，后面看情况重构
 */
class SourceProcessor(
    val name: String,
    // 先这样传入 后面要改
    val sourceId: String,
    private val source: Source<SourcePointer>,
    private val itemFileResolver: ItemFileResolver,
    private val downloader: Downloader,
    private val fileMover: FileMover,
    sourceSavePath: Path,
    private val processingStorage: ProcessingStorage,
    private val category: String?,
    private val tags: Set<String>,
    private val options: ProcessorOptions = ProcessorOptions(),
) : Runnable, AutoCloseable {

    private val directDownloader = DirectDownloader(downloader)
    private val downloadPath = downloader.defaultDownloadPath().toAbsolutePath()
    private val sourceSavePath: Path = sourceSavePath.toAbsolutePath()
    private val filenamePattern = options.filenamePattern
    private val savePathPattern = options.savePathPattern
    private var renameTaskFuture: ScheduledFuture<*>? = null
    private val sourceItemFilters: List<SourceItemFilter> = options.itemFilters
    private val itemContentFilters: List<ItemContentFilter> = options.itemContentFilters
    private val fileContentFilters: List<FileContentFilter> = options.fileFilters
    private val variableProviders = options.variableProviders
    private val processListeners: Map<ListenerMode, List<ProcessListener>> = options.processListeners
    private val taggers: List<FileTagger> = options.fileTaggers
    private val fileReplacementDecider: FileReplacementDecider = options.fileReplacementDecider
    private val fileExistsDetector: FileExistsDetector = options.fileExistsDetector
    private val secondaryFileMover = IncludingTargetPathsFileMover(
        ReadonlyFileMover(fileMover),
        processingStorage,
    )

    private val renamer: Renamer = Renamer(
        options.variableErrorStrategy,
        options.variableReplacers,
        options.variableProcessChain
    )
    private val safeRunner by lazy {
        ProcessorSafeRunner(this)
    }
    private val renameScheduledExecutor by lazy {
        val factory = Thread.ofVirtual().name("rename-task", 1).factory()
        Executors.newScheduledThreadPool(1, factory)
    }
    private val itemChannel = Channel<Process>(options.channelBufferSize)
    private val processorCoroutineScope = CoroutineScope(Dispatchers.Default)
    private var lastTriggerTime: Long? = null

    init {
        scheduleRenameTask()
        if (options.parallelism != 1 && source !is AlwaysLatestSource) {
            /**
             * 因为是基于迭代器模式迭代，单线程下如果SourceItem处理出现异常会根据配置终止当前触发的处理或跳过，到下一次触发会继续处理该异常的Item
             * 并行模式下，某个Item出现异常时虽然会根据配置中止当前触发的处理或跳过，但其他Item可能已经处理成功更新了pointer，导致异常的Item下一次触发可能会被跳过
             * AlwaysLatestSource因为是NullPointer，所以在并行处理上没有问题
             */
            log.warn("Processor:'$name' parallelism:${options.parallelism} > 1, but source is not AlwaysLatestSource, recommend to set parallelism to 1")
        }
        log.debug("Processor:'{}' listeners{}", name, processListeners)
        listenChannel()
    }

    private val maxFilenameLength = run {
        if (downloadPath.notExists()) {
            -1
        } else {
            fsMaxFilenameLengthMapping[Files.getFileStore(downloadPath).type()] ?: -1
        }
    }

    private fun listenChannel() {
        // 希望和Process使用同一个，但ProcessScope的Job取消后要恢复成原来的
        processorCoroutineScope.launch {
            for (process in itemChannel) {
                process.run()
            }
        }
    }

    override fun run() {
        lastTriggerTime = System.currentTimeMillis()
        NormalProcess().run()
    }

    fun getLastTriggerTime(): Long? {
        return lastTriggerTime
    }

    fun dryRun(options: DryRunOptions = DryRunOptions()): List<ProcessingContent> {
        val currentSourceState = currentSourceState()
        val values = options.pointer ?: currentSourceState.lastPointer.values
        val pointer = ProcessorSourceState.resolvePointer(source::class, values)

        val process = DryRunProcess(currentSourceState, pointer, options.filterProcessed)
        process.run()
        return process.getResult()
    }

    suspend fun run(sourceItems: List<SourceItem>) {
        itemChannel.send(ManualItemProcess(sourceItems))
    }

    private fun scheduleRenameTask() {
        if (downloader !is AsyncDownloader) {
            return
        }
        renameTaskFuture?.cancel(false)
        renameTaskFuture = renameScheduledExecutor.scheduleAtFixedRate({
            log.debug("Processor:'$name' 开始重命名任务...")
            var modified = false
            val measureTime = measureTime {
                try {
                    modified = runRename() > 0
                } catch (e: Exception) {
                    log.error("Processor:'$name' 重命名任务出错", e)
                }
                System.currentTimeMillis()
            }

            if (modified) {
                log.info("Processor:'$name' 重命名任务完成 took:${measureTime.inWholeMilliseconds}ms")
            }
            val renameCostTimeThreshold = 150L
            if (modified.not() && measureTime.inWholeMilliseconds > renameCostTimeThreshold) {
                log.warn("Processor:'$name' 重命名任务没有修改 took:${measureTime.inWholeMilliseconds}ms")
            }
        }, 30L, options.renameTaskInterval.seconds, TimeUnit.SECONDS)
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
            "Listeners" to processListeners.map { it::class.simpleName },
            "DownloadPath" to downloadPath,
            "SourceSavePath" to sourceSavePath,
            "FileContentFilter" to fileContentFilters.map { it::class.simpleName },
            "Taggers" to taggers.map { it::class.simpleName },
            "Options" to options,
        )
    }

    private fun info0(): ProcessorInfo {
        return ProcessorInfo(
            name,
            downloadPath,
            sourceSavePath,
            tags.toList(),
            category,
            // 有空加
        )
    }

    private fun cancelItem(
        sourceItem: SourceItem,
        existsFile: CoreFileContent,
        discardedItems: MutableMap<String, Boolean>
    ) {
        val file = existsFile.let {
            SourceFile(it.fileDownloadPath, it.attrs, it.fileUri)
        }
        val hashing = sourceItem.hashing()
        discardedItems.computeIfAbsent(hashing) {
            log.info("Processor:'{}' cancel item:{}", name, sourceItem)
            // cancel整个item不太合理，需要以文件的纬度按需取消
            downloader.cancel(sourceItem, listOf(file))
            true
        }
    }

    private fun createItemContent(sourceItem: SourceItem, itemOptions: ItemSelectedOptions): CoreItemContent {
        val resolvedFiles = itemFileResolver.resolveFiles(sourceItem).map { file ->
            val tags = taggers.mapNotNull { it.tag(file) }.toMutableSet()
            if (tags.isEmpty() && file.tags.isEmpty()) {
                return@map file
            }
            tags.addAll(file.tags)
            file.copy(tags = tags)
        }.map {
            val path = cuttingFilename(it.path, maxFilenameLength)
            if (it.path == path) {
                return@map it
            }
            log.info(
                "Processor:'{}' item:'{}' filename:'{}' is too long, cutting to:'{}'",
                name,
                sourceItem.title,
                it.path,
                path
            )
            it.copy(path)
        }
        checkResolvedFiles(sourceItem, resolvedFiles)

        val variableProvider = VariableProvidersAggregation(
            sourceItem,
            itemOptions.variableProviders.toList(),
            options.variableConflictStrategy,
            options.variableNameReplace
        )
        val itemVariables = variableProvider.itemVariables(sourceItem)
        val itemRenameVariables = renamer.itemRenameVariables(sourceItem, itemVariables)
        log.trace("Processor:'{}' item:{} variables:{}", name, sourceItem, itemVariables)
        val fileContents = resolvedFiles.groupBy {
            options.matchFileOption(it)
        }.flatMap { (fileOption, files) ->
            val fileVariables = variableProvider.fileVariables(sourceItem, itemVariables, files)
            checkFileVariables(files, fileVariables)
            files.mapIndexed { index, file ->
                val rawFileContent = RawFileContent(
                    sourceSavePath,
                    downloadPath,
                    MapPatternVariables(fileVariables[index]),
                    fileOption?.savePathPattern ?: itemOptions.savePathPattern ?: savePathPattern,
                    fileOption?.filenamePattern ?: itemOptions.filenamePattern ?: filenamePattern,
                    file
                )
                val fileContent = renamer.createFileContent(sourceItem, rawFileContent, itemRenameVariables)
                fileContent to fileOption
            }
        }.filter { (fileContent, fileOption) ->
            val filters = fileOption?.fileFilters ?: fileContentFilters
            val filter = filters.all { it.test(fileContent) }
            if (filter.not()) {
                log.trace("Filtered file:{}", fileContent.fileDownloadPath)
            }
            filter
        }.map { it.first }
        return CoreItemContent(sourceItem, fileContents, MapPatternVariables(itemVariables))
    }

    private fun checkFileVariables(files: List<SourceFile>, fileVariables: List<PatternVariables>) {
        if (files.size == fileVariables.size) {
            return
        }
        log.error(
            "Processor:'{}' ItemFileResolver:{} resolved files:{} and fileVariables:{} size not match",
            name, itemFileResolver::class.jvmName, files, fileVariables
        )
        throw IllegalStateException("Resolved files and fileVariables size not match")
    }

    private fun checkResolvedFiles(sourceItem: SourceItem, resolvedFiles: List<SourceFile>) {
        val duplicated = resolvedFiles.groupBy { it.path }
            .filter { it.value.size > 1 }
            .map { it.key }
        if (duplicated.isNotEmpty()) {
            log.error(
                "Processor:'{}' ItemFileResolver:{} resolved item:{} duplicated files:{}, It's likely that there's an issue with the component's implementation.",
                name, itemFileResolver::class.jvmName, sourceItem, duplicated
            )
            throw IllegalStateException("Duplicated files:$duplicated")
        }
    }

    fun runRename(): Int {
        val asyncDownloader = downloader as? AsyncDownloader
        if (asyncDownloader == null) {
            log.warn("Processor:'$name' 非异步下载器不执行重命名任务")
            return 0
        }
        val downloadStatusGrouping = processingStorage.findRenameContent(name, options.renameTimesThreshold)
            .groupBy({ pc ->
                DownloadStatus.from(asyncDownloader.isFinished(pc.itemContent.sourceItem))
            }, { it })

        downloadStatusGrouping[DownloadStatus.NOT_FOUND]?.forEach { pc ->
            kotlin.runCatching {
                log.info("Processor:'{}' item:{} not found, id:{}", name, pc.itemContent.sourceItem, pc.id)
                processingStorage.save(
                    pc.copy(
                        status = DOWNLOAD_FAILED,
                        modifyTime = LocalDateTime.now(),
                    )
                )
                val targetPaths = pc.itemContent.fileContents.map { it.targetPath().toString() }
                val hashing = pc.itemContent.sourceItem.hashing()
                processingStorage.deleteTargetPaths(targetPaths, hashing)
            }.onFailure {
                log.error("Processing更新状态出错, id:{} item:{}", pc.id, pc.itemContent.sourceItem, it)
            }
        }

        val downloadedContents = downloadStatusGrouping[DownloadStatus.FINISHED] ?: emptyList()
        downloadedContents.forEach { pc ->
            kotlin.runCatching {
                processRenameTask(pc)
            }.onFailure {
                log.error("Processing重命名任务出错, id:{} item:{}", pc.id, pc.itemContent.sourceItem, it)
            }
        }
        val context = CoreProcessContext(name, processingStorage, info0())
        downloadedContents.forEach {
            context.touch(it)
        }

        if (downloadedContents.isNotEmpty()) {
            invokeListeners(ListenerMode.BATCH, false) {
                this.onProcessCompleted(context)
            }
        }

        return downloadedContents.size
    }

    private fun processRenameTask(pc: ProcessingContent) {
        val itemContent = pc.itemContent
        val sourceFiles = itemContent.fileContents

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
            log.debug("全部目标文件已存在无需重命名，id:{} item:{}", pc.id, pc.itemContent.sourceItem)
            return
        }

        val context = CoreProcessContext(name, processingStorage, info0())
        itemContent.updateFileStatus(fileMover, fileExistsDetector)
        val success = runCatching {
            val moved = moveFiles(itemContent)
            val replaced = replaceFiles(itemContent)
            moved && replaced
        }.onFailure {
            log.error("重命名出错, id:{} item:{}", pc.id, pc.itemContent.sourceItem, it)
            invokeListeners {
                this.onItemError(context, itemContent.sourceItem, it)
            }
        }.onSuccess {
            if (it) {
                invokeListeners(inProcess = false) {
                    this.onItemSuccess(context, itemContent)
                }
            }
        }.getOrDefault(false)

        val renameTimesThreshold = options.renameTimesThreshold
        if (pc.renameTimes == renameTimesThreshold) {
            log.warn("重命名{}次重试失败, id:{} item:{}", renameTimesThreshold, pc.id, pc.itemContent.sourceItem)
        }
        val toUpdate = pc.copy(
            renameTimes = pc.renameTimes.inc(),
            status = if (success) RENAMED else WAITING_TO_RENAME,
            modifyTime = LocalDateTime.now()
        )
        processingStorage.save(toUpdate)
    }

    private fun saveTargetPaths(sourceItem: SourceItem, paths: List<Path>) {
        val processingTargetPaths = if (fileReplacementDecider == NeverReplace) {
            paths.map { ProcessingTargetPath(it, null, sourceItem.hashing()) }
        } else {
            paths.map { ProcessingTargetPath(it, name, sourceItem.hashing()) }
        }
        processingStorage.saveTargetPaths(processingTargetPaths)
    }

    fun safeTask(): ProcessorTask {
        return ProcessorTask(name, safeRunner, options.taskGroup)
    }

    private fun createDownloadTask(content: CoreItemContent, replaceFiles: List<CoreFileContent>): DownloadTask {
        val downloadFiles = content.downloadableFiles()
            .toMutableList()
            .apply { this.addAll(replaceFiles) }
            .distinct()
        val item = content.sourceItem
        log.debug("Processor:'{}' item:{} create download task, files:{}", name, item.title, downloadFiles)

        val headers = HashMap(source.headers(item))
        headers.putAll(options.downloadOptions.headers)
        val downloadOptions = options.downloadOptions
        return DownloadTask(
            item,
            downloadFiles.map {
                SourceFile(it.fileDownloadPath, it.attrs, it.fileUri, data = it.data)
            },
            downloadPath,
            downloadOptions.copy(headers = headers)
        )
    }

    private fun moveFiles(content: CoreItemContent): Boolean {
        val movableFiles = content.movableFiles()
        if (movableFiles.isEmpty()) {
            log.debug("Processor:'{}' no available files to rename, item:'{}'", name, content.sourceItem)
            return true
        }
        movableFiles.map { it.saveDirectoryPath() }
            .distinct()
            .forEach {
                fileMover.createDirectories(it)
            }

        if (log.isDebugEnabled) {
            movableFiles.forEach {
                if (log.isDebugEnabled) {
                    log.debug("Move file:'${it.fileDownloadPath}' to '${it.targetPath()}'")
                }
            }
        }
        return fileMover.move(
            content.copy(fileContents = movableFiles)
        )
    }

    private fun replaceFiles(itemContent: CoreItemContent): Boolean {
        if (downloader is NoneDownloader) {
            return true
        }

        val replaceableFiles =
            itemContent.fileContents.filter { it.status == FileContentStatus.READY_REPLACE }

        if (replaceableFiles.isEmpty()) {
            return true
        }
        log.info(
            "Processor:'{}' sourceItem:{} replaceFiles:{}",
            name, itemContent.sourceItem.title, replaceableFiles.map { it.targetPath() }
        )

        val replaced = fileMover.replace(itemContent.copy(fileContents = replaceableFiles))
        if (replaced) {
            itemContent.fileContents.filter { it.status == FileContentStatus.READY_REPLACE }
                .onEach { it.status = FileContentStatus.REPLACE }
        }
        return replaced
    }

    override fun toString(): String {
        return info().map {
            "${it.key}: ${it.value}"
        }.joinToString("\n")
    }

    override fun close() {
        renameTaskFuture?.cancel(false)
        renameScheduledExecutor.shutdown()
        processorCoroutineScope.cancel("Processor:$name closed")
        itemChannel.close()
    }

    fun currentSourceState(): ProcessorSourceState {
        return processingStorage.findProcessorSourceState(name, sourceId)
            ?: ProcessorSourceState(
                processorName = name,
                sourceId = sourceId,
                lastPointer = Jackson.convert(source.defaultPointer(), PersistentPointer::class)
            )
    }

    suspend fun reprocess(content: ProcessingContent) {
        if (content.processorName != name) {
            throw IllegalArgumentException("content:${content.id} not belong to processor:$name")
        }
        itemChannel.send(Reprocess(content))
    }

    companion object {

        private val processDispatcher = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("process-task", 1).factory()
        ).asCoroutineDispatcher()

        private val filteredStatuses = setOf(FILTERED, TARGET_ALREADY_EXISTS)
        private val fsMaxFilenameLengthMapping = mapOf(
            "zfs" to 250
        )

        fun cuttingFilename(path: Path, limitByteSize: Int): Path {
            if (limitByteSize <= 0) {
                return path
            }
            val filenameBytes = path.nameWithoutExtension.toByteArray(Charsets.UTF_8)
            if (filenameBytes.size <= limitByteSize) {
                return path
            }

            val decoder = Charsets.UTF_8.newDecoder()
            decoder.onMalformedInput(CodingErrorAction.IGNORE)
            decoder.reset()
            val buf = ByteBuffer.wrap(filenameBytes, 0, limitByteSize)
            val decode = decoder.decode(buf)
            return path.resolveSibling("$decode.${path.extension}")
        }
    }

    private data class ItemSelectedOptions(
        val filters: List<SourceItemFilter>,
        val variableProviders: List<VariableProvider>,
        val filenamePattern: CorePathPattern?,
        val savePathPattern: CorePathPattern?,
    )

    private fun getRetryPolicy(stage: String): RetryPolicy<Throwable> {
        val ioExceptionPredicate = continueIf<Throwable> {
            val failure = it.failure
            val isContinue = failure is IOException
            if (isContinue && it.number > 0) {
                log.warn(
                    "第{}次重试失败, {}, message:{}",
                    it.number,
                    stage,
                    "${failure::class.simpleName}:${failure.message}"
                )
            }
            isContinue
        }
        return stopAtAttempts<Throwable>(4) + ioExceptionPredicate + constantDelay(options.retryBackoffMills)
    }

    private abstract inner class Process(
        protected val sourceState: ProcessorSourceState = currentSourceState(),
        protected val sourcePointer: SourcePointer = ProcessorSourceState.resolvePointer(
            source::class,
            sourceState.lastPointer.values
        ),
        private val customIterable: Iterable<PointedItem<ItemPointer>>? = null,
        val context: CoreProcessContext = CoreProcessContext(name, processingStorage, info0())
    ) {

        protected val processLock: Lock = if (options.parallelism > 1) ReentrantLock(true) else NoLock
        protected val processScope = CoroutineScope(processDispatcher)

        @OptIn(InternalCoroutinesApi::class)
        protected suspend fun processItems() {
            secondaryFileMover.releaseAll()
            val stat = context.stat
            stat.stopWatch.start("fetch-items")

            val itemIterable = retry(getRetryPolicy("fetch-source-items")) {
                source.fetch(sourcePointer, options.fetchLimit)
            }
            stat.stopWatch.stop()
            stat.stopWatch.start("process-items")

            val semaphore = Semaphore(options.parallelism, true)
            // 处理Source的迭代器返回重复的Item
            val itemSet = Collections.synchronizedSet(mutableSetOf<SourceItem>())
            val processJob = processScope.launch process@{
                for (pointed in itemIterable) {
                    semaphore.acquire()
                    if (this.coroutineContext.job.isCancelled) {
                        break
                    }

                    val sourceItem = pointed.sourceItem
                    if (itemSet.contains(sourceItem)) {
                        log.debug("Processor:'{}' source given duplicate item:{}", name, sourceItem)
                        semaphore.release()
                        continue
                    }

                    log.trace("Processor:'{}' launch item:{}", name, pointed)
                    itemSet.add(sourceItem)
                    launch {
                        log.trace("Processor:'{}' start process item:{}", name, pointed)
                        val processingContent = runCatching {
                            val itemOptions = selectItemOptions(pointed)
                            val filtered = filterItem(itemOptions, pointed)
                            if (filtered) {
                                itemSet.remove(sourceItem)
                                stat.incFilterCounting()
                                semaphore.release()
                                return@launch
                            }
                            val ct = processWithRetry(pointed, itemOptions)
                            context.touch(ct)
                            onItemSuccess(pointed, ct)
                            ct
                        }.onFailure {
                            onItemError(pointed.sourceItem, it)
                            if (it is ProcessingException && it.skip) {
                                log.error("Processor:'$name'处理失败, item:$pointed, 被组件定义为可跳过的异常")
                                return@onFailure
                            }
                            log.error("Processor:'$name'处理失败, item:$pointed", it)

                            if (options.itemErrorContinue.not()) {
                                log.warn("Processor:'$name'处理失败, item:$pointed, 退出本次触发处理, 如果未能解决该处理器将无法继续处理后续Item")
                                processScope.cancel(
                                    CancellationException("由于Item:${sourceItem}处理失败，退出本次触发处理", null)
                                )
                                semaphore.release()
                                return@launch
                            }
                        }.getOrElse {
                            val errorContent =
                                ProcessingContent(name, CoreItemContent(sourceItem, emptyList(), MapPatternVariables()))
                                    .copy(status = FAILURE, failureReason = it.message)
                            whenItemErrorReturning(errorContent, it)
                        }

                        try {
                            onItemCompleted(processingContent)
                            log.trace("Processor:'{}' finished process item:{}", name, pointed)
                            releasePaths(processingContent)
                        } finally {
                            itemSet.remove(sourceItem)
                            semaphore.release()
                        }
                        if (filteredStatuses.contains(processingContent.status)) {
                            stat.incFilterCounting()
                        } else {
                            stat.incProcessingCounting()
                        }
                    }

                }
            }

            processJob.join()

            secondaryFileMover.releaseAll()
            if (processJob.isCancelled) {
                val ex = processJob.getCancellationException()
                log.info("Processor:'{}' process job cancelled, message:{}", name, ex.message)
            }

            stat.stopWatch.stop()
            onProcessCompleted(context)
            if (stat.hasChange()) {
                log.info("Processor:{}", stat)
            }
        }

        protected open fun whenItemErrorReturning(
            errorContent: ProcessingContent,
            throwable: Throwable
        ): ProcessingContent {
            return errorContent
        }

        private suspend fun processWithRetry(
            pointed: PointedItem<ItemPointer>, itemOptions: ItemSelectedOptions,
        ): ProcessingContent {
            val sourceItem = pointed.sourceItem
            return flowOf(sourceItem)
                .map { processItem(it, itemOptions) }
                .retryWhen { cause, attempt ->
                    secondaryFileMover.release(sourceItem)
                    val retry = cause is IOException && attempt < 3
                    if (retry) {
                        delay(options.retryBackoffMills)
                    }
                    retry
                }.first()
        }

        private fun filterItem(
            itemOptions: ItemSelectedOptions,
            item: PointedItem<ItemPointer>,
        ): Boolean {
            val filterBy = itemOptions.filters.firstOrNull { it.test(item.sourceItem).not() }
            if (filterBy != null) {
                log.debug("{} filtered item:{}", filterBy::class.simpleName, item.sourceItem)
                onItemFiltered(item)
                return true
            }
            return false
        }

        private fun releasePaths(processingContent: ProcessingContent) {
            val sourceItem = processingContent.itemContent.sourceItem
            context.removeItemPaths(sourceItem)
            if (processingContent.status == FAILURE) {
                secondaryFileMover.release(sourceItem)
                return
            }
            val paths = processingContent.itemContent.downloadableFiles().map {
                it.targetPath()
            }
            if (paths.isEmpty()) {
                // 提前占用了但是处理时失败需要释放该Item下的路径
                secondaryFileMover.release(sourceItem)
            } else {
                secondaryFileMover.release(sourceItem, paths)
            }
        }

        private fun selectItemOptions(item: PointedItem<ItemPointer>): ItemSelectedOptions {
            val itemOption = options.matchItemOption(item.sourceItem)
            val itemFilters = itemOption?.itemFilters ?: this.selectItemFilters()
            val itemVariableProviders = itemOption?.variableProviders ?: options.variableProviders
            return ItemSelectedOptions(
                itemFilters,
                itemVariableProviders,
                itemOption?.filenamePattern,
                itemOption?.savePathPattern
            )
        }

        protected open fun selectItemFilters(): List<SourceItemFilter> {
            return sourceItemFilters
        }

        fun processItem(sourceItem: SourceItem, itemOptions: ItemSelectedOptions): ProcessingContent {
            val itemContent = createItemContent(sourceItem, itemOptions)
            val filterBy = itemContentFilters.firstOrNull { it.test(itemContent).not() }
            if (filterBy != null) {
                log.debug("Processor:'{}' {} filtered item:{}", name, filterBy::class.simpleName, sourceItem)
                return ProcessingContent(name, itemContent).copy(status = FILTERED)
            }
            val itemContext = ItemContext()
            val mover = this.selectUpdateStatusMover()
            var replaceFiles: List<CoreFileContent> = emptyList()
            processLock.lock {
                itemContent.updateFileStatus(mover, fileExistsDetector)
                val downloadableTargetPaths = itemContent.downloadableFiles().map { it.targetPath() }
                if (log.isDebugEnabled) {
                    val filesStatus = itemContent.downloadableFiles().map {
                        it.targetFilename to it.status
                    }
                    log.debug("Processor:'{}' item:{} file status:{}", name, sourceItem.title, filesStatus)
                }
                // 在处理完后释放
                val targetPaths = downloadableTargetPaths + replaceFiles.map { it.targetPath() }
                secondaryFileMover.preoccupiedTargetPath(sourceItem, targetPaths)
                replaceFiles = identifyFilesToReplace(itemContent)
                context.addItemPaths(itemContent, targetPaths)

                val (shouldDownload, contentStatus) = probeContentStatus(itemContent, replaceFiles)
                itemContext.shouldDownload = shouldDownload
                itemContext.status = contentStatus
                if (context.isItemCanceled(sourceItem)) {
                    itemContext.status = CANCELLED
                }
                log.trace(
                    "Processor:'{}' item:{}, shouldDownload: {}, contentStatus:{}",
                    name, sourceItem, shouldDownload, contentStatus
                )
                val processingContent = postProcessingContent(
                    ProcessingContent(name, itemContent).copy(status = itemContext.status)
                )
                itemContext.content = processingContent
            }
            val shouldDownload = itemContext.shouldDownload
            val contentStatus = itemContext.status
            val processingContent = itemContext.content
            checkNotNull(processingContent)

            if (shouldDownload) {
                log.info("Processor:'{}' start download item:{}", name, sourceItem)
                val success = doDownload(processingContent, replaceFiles)
                if (success && downloader !is AsyncDownloader) {
                    log.trace("Processor:'{}' start rename item:{}", name, sourceItem)
                    val (moveSuccess, replaceSuccess) = doMovement(itemContent)
                    if (moveSuccess || replaceSuccess) {
                        return processingContent.copy(status = RENAMED, renameTimes = 1)
                    }
                    return processingContent.copy(status = FAILURE)
                }
            }

            if (contentStatus == WAITING_TO_RENAME && downloader !is AsyncDownloader && this !is DryRunProcess) {
                val (moveSuccess, replaceSuccess) = doMovement(itemContent)
                if (moveSuccess || replaceSuccess) {
                    return processingContent.copy(status = RENAMED, renameTimes = 1)
                }
            }

            return processingContent
        }

        private fun identifyFilesToReplace(currentItem: CoreItemContent): List<CoreFileContent> {
            if (fileReplacementDecider == NeverReplace) {
                return emptyList()
            }

            val existsContentFiles = currentItem.fileContents
                .filter { it.status == FileContentStatus.TARGET_EXISTS && it.existTargetPath != null }
            val support = TargetPathRelationSupport(
                currentItem.sourceItem,
                existsContentFiles,
                processingStorage
            )
            val existTargets = existsContentFiles.mapNotNull { it.existTargetPath }
            val filesToCheck = fileMover.exists(existTargets).zip(existsContentFiles)
            val discardedItems = mutableMapOf<String, Boolean>()
            val replaceFiles = filesToCheck
                .map { (physicalExists, fileContent) ->
                    // 正常情况下不会出现existTargetPath为null的情况
                    val existTargetPath = fileContent.existTargetPath
                        ?: throw IllegalStateException("ExistTargetPath is null")
                    // 预防真实路径还不存在的情况，在(target_path_record)中提前占用的文件
                    val existingFile = if (physicalExists) {
                        existTargetPath.let {
                            fileMover.pathMetadata(it)
                        }
                    } else {
                        SourceFile(existTargetPath)
                    }

                    val physicalBeforeItem = support.getBeforeContent(existingFile.path)
                    val replace = fileReplacementDecider.isReplace(
                        currentItem.copy(
                            fileContents = listOf(fileContent)
                        ),
                        physicalBeforeItem?.itemContent,
                        existingFile
                    )
                    if (replace) {
                        // 只取消正在下载阶段的
                        if (physicalBeforeItem != null) {
                            if (physicalBeforeItem.status == WAITING_TO_RENAME) {
                                log.info("Processor:'{}' cancel physical item:{}", name, physicalBeforeItem)
                                cancelItem(physicalBeforeItem.itemContent.sourceItem, fileContent, discardedItems)
                                processingStorage.save(
                                    physicalBeforeItem.copy(status = CANCELLED, modifyTime = LocalDateTime.now())
                                )
                            }
                        } else {
                            context.findItems(existTargetPath)
                                .filter { it.sourceItem != currentItem.sourceItem }
                                .forEach { content ->
                                    log.info("Processor:'{}' cancel processing item:{}", name, content.sourceItem)
                                    fileContent.status = FileContentStatus.REPLACED
                                    content.fileContents.filter { it.targetPath() == existTargetPath }
                                        .onEach { it.status = FileContentStatus.REPLACED }
                                    cancelItem(content.sourceItem, fileContent, discardedItems)
                                    context.cancelItem(content.sourceItem)
                                }
                        }
                    }
                    fileContent to replace
                }.filter { (_, replace) -> replace }
                .map { (fc, _) -> fc }
                .onEach {
                    it.status = FileContentStatus.READY_REPLACE
                }
            return replaceFiles
        }

        /**
         * @return Download or not, [ProcessingContent.Status]
         */
        private fun probeContentStatus(
            sc: CoreItemContent,
            replaceFiles: List<CoreFileContent>
        ): Pair<Boolean, ProcessingContent.Status> {
            val files = sc.fileContents
            if (files.isEmpty()) {
                return false to NO_FILES
            }

            // 返回true是因为需要做后续的处理
            if (replaceFiles.isNotEmpty()) {
                return true to WAITING_TO_RENAME
            }

            // 预防这一批次的Item有相同的目标，并且是AsyncDownloader的情况下会重复下载
            if (files.all { it.status == FileContentStatus.TARGET_EXISTS }) {
                log.debug(
                    "Item files already exists:{}, files:{}",
                    sc.sourceItem,
                    sc.fileContents.map { it.targetPath() })
                return false to TARGET_ALREADY_EXISTS
            }

            // SystemFileSource下载不需要做任何事情，因为本身就已经存在了
            if (downloader is SystemFileSource) {
                return true to WAITING_TO_RENAME
            }
            val allExists = fileMover.exists(files.map { it.fileDownloadPath }).all { it }
            return if (allExists) {
                // 这里待定，有些下载器重复提交不会有问题
                (downloader is AsyncDownloader) to WAITING_TO_RENAME
            } else {
                true to WAITING_TO_RENAME
            }
        }

        open fun selectUpdateStatusMover(): FileMover {
            return secondaryFileMover
        }

        private fun doMovement(itemContent: CoreItemContent): Pair<Boolean, Boolean> {
            val moveSuccess = moveFiles(itemContent)
            val replaceSuccess = replaceFiles(itemContent)
            return Pair(moveSuccess, replaceSuccess)
        }

        open fun postProcessingContent(processingContent: ProcessingContent): ProcessingContent {
            return processingContent
        }

        open fun onProcessCompleted(processContext: ProcessContext) {}

        open fun onItemCompleted(processingContent: ProcessingContent) {}

        open fun onItemSuccess(item: PointedItem<ItemPointer>, processingContent: ProcessingContent) {}

        open fun onItemFiltered(item: PointedItem<ItemPointer>) {}

        open fun onItemError(item: SourceItem, throwable: Throwable) {}

        open fun doDownload(pc: ProcessingContent, replaceFiles: List<CoreFileContent>): Boolean {
            val itemContent = pc.itemContent
            val downloadTask = createDownloadTask(itemContent, replaceFiles)
            // NOTE 非异步下载器会阻塞
            log.debug(
                "Processor:'{}' submit download item:{}, count:{}, files:{}",
                name,
                downloadTask.sourceItem,
                downloadTask.downloadFiles.size,
                downloadTask.downloadFiles
            )
            val submit = directDownloader.submit(downloadTask)
            val targetPaths = itemContent.downloadableFiles().map { it.targetPath() }
            if (options.saveProcessingContent) {
                saveTargetPaths(itemContent.sourceItem, targetPaths)
            }
            // Events.post(ProcessorSubmitDownloadEvent(name, itemContent))
            return submit
        }

        fun run() {
            runBlocking {
                processItems()
            }
        }

        suspend fun runAsync() {
            processItems()
        }
    }

    private data class ItemContext(
        var shouldDownload: Boolean = false,
        var status: ProcessingContent.Status = NO_FILES,
        var content: ProcessingContent? = null
    )

    private fun invokeListeners(
        mode: ListenerMode = ListenerMode.EACH,
        inProcess: Boolean = true,
        block: ProcessListener.() -> Unit
    ) {
        if (inProcess && downloader is AsyncDownloader) {
            log.trace("Processor:'{}' async downloader not invoke listeners in process", name)
            return
        }
        val targetListeners = processListeners[mode] ?: emptyList()
        if (targetListeners.isEmpty()) {
            log.trace("Processor:'{}' no listeners", name)
            return
        }
        log.trace("Processor:'{}' invoke listeners", name)
        for (listener in targetListeners) {
            listener.runCatching {
                block.invoke(this)
            }.onFailure {
                log.error("${listener::class.simpleName}发生错误", it)
            }
        }
    }

    fun dryRunStream(options: DryRunOptions): Flow<ProcessingContent> {
        val currentSourceState = currentSourceState()
        val values = options.pointer ?: currentSourceState.lastPointer.values
        val pointer = ProcessorSourceState.resolvePointer(source::class, values)
        val process = DryRunStreamProcess(currentSourceState, pointer, options.filterProcessed)

        processorCoroutineScope.launch {
            process.runAsync()
        }
        return process.getStream()
    }

    private inner class NormalProcess : Process() {

        override fun onProcessCompleted(processContext: ProcessContext) {
            if (processContext.processedItems().isNotEmpty()) {
                invokeListeners(ListenerMode.BATCH) {
                    this.onProcessCompleted(processContext)
                }
            }
            if (options.pointerBatchMode) {
                saveSourceState()
            } else if (processContext.processedItems().isEmpty()) {
                saveSourceState()
            }
        }

        override fun onItemCompleted(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                // 后面优化
                if (processingContent.status == FILTERED) {
                    return
                }
                processingStorage.save(processingContent)
            }

            val warningFiles = processingContent.itemContent.fileContents.filter { it.status.isWarning() }
            if (warningFiles.isNotEmpty()) {
                log.debug(
                    "Processor:'{}' has warning status, item:{} files:{}",
                    name,
                    processingContent.itemContent.sourceItem,
                    warningFiles.map {
                        it.targetPath() to it.status
                    }
                )
            }
        }

        override fun onItemSuccess(item: PointedItem<ItemPointer>, processingContent: ProcessingContent) {
            invokeListeners {
                this.onItemSuccess(context, processingContent.itemContent)
            }

            processLock.lock {
                sourcePointer.update(item.pointer)
            }

            if (log.isTraceEnabled) {
                log.trace("Processor:'{}' on success update pointer:{}", name, item.pointer)
            }
            if (options.pointerBatchMode.not()) {
                saveSourceState()
            }
        }

        override fun onItemError(item: SourceItem, throwable: Throwable) {
            invokeListeners {
                this.onItemError(context, item, throwable)
            }
        }

        override fun onItemFiltered(item: PointedItem<ItemPointer>) {
            processLock.lock {
                sourcePointer.update(item.pointer)
            }
            if (log.isTraceEnabled) {
                log.trace("Processor:'{}' on filtered update pointer:{}", name, item.pointer)
            }
            if (options.pointerBatchMode.not()) {
                saveSourceState()
            }
        }

        /**
         * 并行执行还未实现按照迭代器的顺序保存
         */
        private fun saveSourceState() {
            processLock.lock {
                val currentSourceState = sourceState.copy(
                    lastPointer = Jackson.convert(sourcePointer, PersistentPointer::class),
                    lastActiveTime = LocalDateTime.now()
                )

                val lastP = ProcessorSourceState.resolvePointer(source::class, sourceState.lastPointer.values)
                val currP = sourcePointer
                if (currP != lastP) {
                    try {
                        val before = Jackson.convert<JsonNode>(lastP)
                        val current = Jackson.convert<JsonNode>(currP)
                        val difference = JsonComparator.findDifference(before, current)
                        if (difference !is NullNode) {
                            log.info("Processor:'{}' source pointer changed:{}", name, difference)
                        }
                    } catch (e: Exception) {
                        log.warn("Processor:'{}' source pointer find diff error", name, e)
                    }
                }
                val save = processingStorage.save(currentSourceState)
                sourceState.id = save.id
            }
        }
    }

    private open inner class DryRunProcess(
        sourceState: ProcessorSourceState,
        sourcePointer: SourcePointer,
        private val filterProcessed: Boolean
    ) : Process(sourceState, sourcePointer) {

        private val result: MutableList<ProcessingContent> = mutableListOf()

        override fun selectItemFilters(): List<SourceItemFilter> {
            if (filterProcessed) {
                return super.selectItemFilters()
            }
            return super.selectItemFilters().filter { it !is SourceHashingItemFilter }
        }

        override fun onItemCompleted(processingContent: ProcessingContent) {
            result.add(processingContent)
        }

        fun getResult(): List<ProcessingContent> {
            return result
        }

        override fun doDownload(pc: ProcessingContent, replaceFiles: List<CoreFileContent>): Boolean = false
    }

    private inner class DryRunStreamProcess(
        sourceState: ProcessorSourceState,
        sourcePointer: SourcePointer,
        filterProcessed: Boolean
    ) : DryRunProcess(sourceState, sourcePointer, filterProcessed) {

        private val resultStream: Channel<ProcessingContent> = Channel()

        override fun onItemCompleted(processingContent: ProcessingContent) {
            runBlocking {
                resultStream.send(processingContent)
            }
        }

        fun getStream(): Flow<ProcessingContent> {
            return resultStream.consumeAsFlow().onCompletion {
                processScope.cancel("DryRunStreamProcess completed")
            }
        }

        override fun onProcessCompleted(processContext: ProcessContext) {
            resultStream.close()
        }
    }

    private inner class ManualItemProcess(items: List<SourceItem>) : Process(
        customIterable = items.map {
            PointedItem(it, NullPointer)
        }) {

        override fun onItemCompleted(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                processingStorage.save(processingContent)
            }
        }
    }

    private inner class Reprocess(
        private val content: ProcessingContent
    ) : Process(
        customIterable = listOf(PointedItem(content.itemContent.sourceItem, NullPointer))
    ) {

        override fun postProcessingContent(processingContent: ProcessingContent): ProcessingContent {
            return processingContent.copy(id = content.id, processorName = name, renameTimes = 0)
        }

        override fun onItemCompleted(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                processingStorage.save(processingContent)
            }
        }

        override fun selectItemFilters(): List<SourceItemFilter> {
            return super.selectItemFilters().filter { it !is SourceHashingItemFilter }
        }

        override fun selectUpdateStatusMover(): FileMover {
            // 重新处理只检查真实存在的文件
            return fileMover
        }

        override fun whenItemErrorReturning(errorContent: ProcessingContent, throwable: Throwable): ProcessingContent {
            return errorContent.copy(id = content.id)
        }
    }

}

val log: Logger = LoggerFactory.getLogger("SourceProcessor")