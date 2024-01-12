package io.github.shoaky.sourcedownloader.core.processor

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
import io.github.shoaky.sourcedownloader.util.LoggingStageRetryListener
import io.github.shoaky.sourcedownloader.util.NoLock
import io.github.shoaky.sourcedownloader.util.lock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.util.StopWatch
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
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
    private val processListeners: List<ProcessListener> = options.processListeners
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

    init {
        scheduleRenameTask()
        if (options.parallelism != 1 && source !is AlwaysLatestSource) {
            /**
             * 因为是基于迭代器模式迭代，增加并行后Source组件的Pointer变得难以维护并且处理器State的存储存在并发问题暂时没有经过严格测试
             * AlwaysLatestSource因为是NullPointer，所以在并行处理上没有问题
             */
            log.warn("Processor:'$name' parallelism:${options.parallelism} > 1, but source is not AlwaysLatestSource, recommend to set parallelism to 1")
        }
        if (options.fileReplacementDecider !is NeverReplace) {
            log.warn("Processor:'$name' fileReplacementDecider:${options.fileReplacementDecider} is not NeverReplace, may have unexpected results")
        }
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
        NormalProcess().run()
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
            options.tags.toList(),
            options.category,
            // 有空加
        )
    }

    private fun identifyFilesToReplace(itemContent: CoreItemContent): List<CoreFileContent> {
        if (fileReplacementDecider == NeverReplace) {
            return emptyList()
        }

        val existsContentFiles = itemContent.sourceFiles
            .filter { it.status == FileContentStatus.TARGET_EXISTS && it.existTargetPath != null }
        val support = TargetPathRelationSupport(
            itemContent.sourceItem,
            existsContentFiles,
            processingStorage
        )
        val existTargets = existsContentFiles.mapNotNull { it.existTargetPath }
        val filesToCheck = fileMover.exists(existTargets).zip(existsContentFiles)
        val discardedItems = mutableMapOf<String, Boolean>()
        val replaceFiles = filesToCheck
            .map { (physicalExists, fileContent) ->
                val existTargetPath =
                    fileContent.existTargetPath ?: throw IllegalStateException("ExistTargetPath is null")
                // 预防真实路径还不存在的情况，在(target_path_record)中提前占用的文件
                val existingFile = if (physicalExists) {
                    existTargetPath.let {
                        fileMover.pathMetadata(it)
                    }
                } else {
                    SourceFile(existTargetPath)
                }

                val before = support.getBeforeContent(existingFile.path)
                val replace = fileReplacementDecider.isReplace(
                    itemContent.copy(
                        sourceFiles = listOf(fileContent)
                    ),
                    before?.itemContent,
                    existingFile
                )
                if (replace) {
                    // 这里没有维护被替换Item的数据
                    cancelSubmittedProcessing(before, fileContent, discardedItems)
                }
                fileContent to replace
            }.filter { it.second }
            .map { it.first }
            .onEach {
                it.status = FileContentStatus.READY_REPLACE
            }
        return replaceFiles
    }

    // 问题比较多
    // 1.如果目标文件本身已经存在，但ProcessingContent是null
    private fun cancelSubmittedProcessing(
        before: ProcessingContent?,
        existsFile: CoreFileContent,
        discardedItems: MutableMap<String, Boolean>
    ) {
        log.info(
            "Processor:'{}' cancel before processing, existsFile:{}, discardedItems:{}",
            name,
            existsFile,
            discardedItems
        )

        // TODO 支持并行模式
        // 虽然计算出已存在，但并行模式ProcessingContent可能还未入库，需要一个ProcessContext来获取冲突的ItemContent
        if (before == null || before.status != WAITING_TO_RENAME) {
            log.info("Processor:'{}' before task is null or not waiting to rename, existsFile:{}", name, existsFile)
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

        val sourceItemGroup = VariableProvidersAggregation(
            sourceItem,
            itemOptions.variableProviders.filter { it.support(sourceItem) }.toList(),
            options.variableConflictStrategy,
            options.variableNameReplace
        )
        val sharedPatternVariables = sourceItemGroup.sharedPatternVariables()
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
                    fileOption?.savePathPattern ?: itemOptions.savePathPattern ?: savePathPattern,
                    fileOption?.filenamePattern ?: itemOptions.filenamePattern ?: filenamePattern,
                    file
                )
                val fileContent = renamer.createFileContent(sourceItem, rawFileContent, sharedPatternVariables)
                fileContent to fileOption
            }
        }.filter { (fileContent, fileOption) ->
            val filters = fileOption?.fileContentFilters ?: fileContentFilters
            val filter = filters.all { it.test(fileContent) }
            if (filter.not()) {
                log.trace("Filtered file:{}", fileContent.fileDownloadPath)
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
            log.error(
                "Processor:'{}' ItemFileResolver:{} resolved item:{} duplicated files:{}, It's likely that there's an issue with the component's implementation.",
                name, itemFileResolver::class.jvmName, sourceItem, duplicated
            )
            throw IllegalStateException("Duplicated files:$duplicated")
        }
    }

    /**
     * @return Download or not, [ProcessingContent.Status]
     */
    private fun probeContentStatus(
        sc: CoreItemContent,
        replaceFiles: List<CoreFileContent>
    ): Pair<Boolean, ProcessingContent.Status> {
        val files = sc.sourceFiles
        if (files.isEmpty()) {
            return false to NO_FILES
        }

        // 返回true是因为需要做后续的处理
        if (replaceFiles.isNotEmpty()) {
            return true to WAITING_TO_RENAME
        }

        // 预防这一批次的Item有相同的目标，并且是AsyncDownloader的情况下会重复下载
        if (files.all { it.status == FileContentStatus.TARGET_EXISTS }) {
            log.info("Item files already exists:{}, files:{}", sc.sourceItem, sc.sourceFiles.map { it.targetPath() })
            return false to TARGET_ALREADY_EXISTS
        }

        // SystemFileSource下载不需要做任何事情，因为本身就已经存在了
        if (downloader is SystemFileSource) {
            return true to WAITING_TO_RENAME
        }
        val allExists = fileMover.exists(files.map { it.fileDownloadPath }).all { it }
        return if (allExists) {
            false to WAITING_TO_RENAME
        } else {
            true to WAITING_TO_RENAME
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
                val targetPaths = pc.itemContent.sourceFiles.map { it.targetPath() }
                val hashing = pc.itemContent.sourceItem.hashing()
                processingStorage.deleteTargetPath(targetPaths, hashing)
            }.onFailure {
                log.error("Processing更新状态出错, record:${Jackson.toJsonString(pc)}", it)
            }
        }

        val downloadedContents = downloadStatusGrouping[DownloadStatus.FINISHED] ?: emptyList()
        downloadedContents.forEach { pc ->
            kotlin.runCatching {
                processRenameTask(pc)
            }.onFailure {
                log.error("Processing重命名任务出错, record:${Jackson.toJsonString(pc)}", it)
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
        val success = runCatching {
            val moved = moveFiles(itemContent)
            val replaced = replaceFiles(itemContent)
            moved || replaced
        }.onFailure {
            log.error("重命名出错, record:${Jackson.toJsonString(pc)}", it)
            invokeListeners {
                this.onItemError(itemContent.sourceItem, it)
            }
        }.onSuccess {
            if (it) {
                invokeListeners(inProcess = false) {
                    this.onItemSuccess(itemContent)
                }
            }
        }.getOrDefault(false)

        val renameTimesThreshold = options.renameTimesThreshold
        if (pc.renameTimes == renameTimesThreshold) {
            log.warn("重命名${renameTimesThreshold}次重试失败, record:${Jackson.toJsonString(pc)}")
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

    fun safeTask(): Runnable {
        return safeRunner
    }

    private fun createDownloadTask(content: CoreItemContent, replaceFiles: List<CoreFileContent>): DownloadTask {
        val downloadFiles = content.downloadableFiles()
            .toMutableList()
            .apply { this.addAll(replaceFiles) }
            .distinct()
        if (log.isDebugEnabled) {
            log.debug(
                "Processor:'{}' item:{} create download task, files:{}",
                name,
                content.sourceItem.title,
                downloadFiles
            )
        }

        val headers = HashMap(source.headers())
        headers.putAll(options.downloadOptions.headers)
        val downloadOptions = options.downloadOptions
        return DownloadTask(
            content.sourceItem,
            downloadFiles.map {
                SourceFile(it.fileDownloadPath, it.attrs, it.fileUri, data = it.data)
            },
            downloadPath,
            downloadOptions.copy(headers = headers)
        )
    }

    private fun moveFiles(content: CoreItemContent): Boolean {
        if (downloader is NoneDownloader) {
            return true
        }

        val movableFiles = content.movableFiles()
        if (movableFiles.isEmpty()) {
            log.info("Processor:'$name' no available files to rename, item:'${content.sourceItem}'")
            return false
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
            content.copy(sourceFiles = movableFiles)
        )
    }

    private fun replaceFiles(itemContent: CoreItemContent): Boolean {
        if (downloader is NoneDownloader) {
            return true
        }

        val replaceableFiles =
            itemContent.sourceFiles.filter { it.status == FileContentStatus.READY_REPLACE }

        if (replaceableFiles.isEmpty()) {
            return true
        }
        log.info(
            "Processor:'{}' sourceItem:{} replaceFiles:{}",
            name, itemContent.sourceItem.title, replaceableFiles.map { it.targetPath() }
        )

        val replaced = fileMover.replace(itemContent.copy(sourceFiles = replaceableFiles))
        if (replaced) {
            itemContent.sourceFiles.filter { it.status == FileContentStatus.READY_REPLACE }
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

        private val retry = RetryTemplateBuilder()
            .maxAttempts(3)
            .fixedBackoff(Duration.ofSeconds(5L).toMillis())
            .retryOn(IOException::class.java)
            .traversingCauses()
            .withListener(LoggingStageRetryListener())
            .build()

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
        val item: PointedItem<ItemPointer>,
        val filters: List<SourceItemFilter>,
        val variableProviders: List<VariableProvider>,
        val filenamePattern: CorePathPattern?,
        val savePathPattern: CorePathPattern?,
    )

    private abstract inner class Process(
        protected val sourceState: ProcessorSourceState = currentSourceState(),
        protected val sourcePointer: SourcePointer = ProcessorSourceState.resolvePointer(
            source::class,
            sourceState.lastPointer.values
        ),
        private val customIterable: Iterable<PointedItem<ItemPointer>>? = null
    ) {

        protected val processLock: Lock = if (options.parallelism > 1) ReentrantLock(true) else NoLock

        protected fun processItems() {
            val stat = ProcessStat(name)
            val context = CoreProcessContext(name, processingStorage, info0())
            secondaryFileMover.releaseAll()

            stat.stopWatch.start("fetchItems")
            val itemIterable = customIterable ?: retry.execute<Iterable<PointedItem<ItemPointer>>, IOException> {
                it.setAttribute("stage", ProcessStage("FetchSourceItems", sourceState))
                source.fetch(sourcePointer, options.fetchLimit)
            }
            stat.stopWatch.stop()

            stat.stopWatch.start("processItems")
            val processChannel = Channel<PointedItem<ItemPointer>>(options.parallelism)
            val processScope = CoroutineScope(Dispatchers.Default)
            val processJob = processScope.launch process@{
                for (item in itemIterable) {
                    processChannel.send(item)
                    log.trace("Processor:'{}' send item to channel:{}", name, item)
                    launch {
                        log.trace("Processor:'{}' start process item:{}", name, item)
                        val itemOptions = selectItemOptions(item)
                        val filterBy = itemOptions.filters.firstOrNull { it.test(item.sourceItem).not() }
                        if (filterBy != null) {
                            log.debug("{} filtered item:{}", filterBy::class.simpleName, item.sourceItem)
                            onItemFiltered(item)
                            stat.incFilterCounting()
                            processChannel.receive()
                            return@launch
                            // continue
                        }
                        val processingContent = runCatching {
                            retry.execute<ProcessingContent, IOException> {
                                it.setAttribute("stage", ProcessStage("ProcessItem", item))
                                processItem(item.sourceItem, itemOptions)
                            }
                        }.onFailure {
                            log.error("Processor:'$name'处理失败, item:$item", it)
                            onItemError(item.sourceItem, it)

                            if (it is ProcessingException && it.skip) {
                                log.error("Processor:'$name'处理失败, item:$item, 被组件定义为可跳过的异常")
                                return@onFailure
                            }

                            if (options.itemErrorContinue.not()) {
                                log.warn("Processor:'$name'处理失败, item:$item, 退出本次触发处理, 如果未能解决该处理器将无法继续处理后续Item")
                                secondaryFileMover.releaseAll()
                                processChannel.cancel()
                                return@launch
                                // return
                            }
                        }.onSuccess {
                            onItemSuccess(item, it)
                        }.getOrElse {
                            ProcessingContent(
                                name, CoreItemContent(
                                    item.sourceItem, emptyList(), MapPatternVariables()
                                )
                            ).copy(status = FAILURE, failureReason = it.message)
                        }

                        try {
                            context.touch(processingContent)
                            onItemCompleted(processingContent)
                            log.trace("Processor:'{}' finished process item:{}", name, item)

                            if (filteredStatuses.contains(processingContent.status)) {
                                stat.incFilterCounting()
                            } else {
                                stat.incProcessingCounting()
                            }
                        } finally {
                            processChannel.receive()
                        }
                    }
                }
            }

            runBlocking {
                processJob.join()
            }
            secondaryFileMover.releaseAll()
            if (processJob.isCancelled) {
                log.info("Processor:'{}' process job cancelled", name)
            }

            stat.stopWatch.stop()
            onProcessCompleted(context)
            if (stat.hasChange()) {
                log.info("Processor:{}", stat)
            }
        }

        private fun selectItemOptions(item: PointedItem<ItemPointer>): ItemSelectedOptions {
            val itemOption = options.matchItemOption(item.sourceItem)
            val itemFilters = itemOption?.sourceItemFilters ?: this.selectItemFilters()
            val itemVariableProviders = itemOption?.variableProviders ?: options.variableProviders
            return ItemSelectedOptions(
                item,
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
                log.info("Processor:'{}' {} filtered item:{}", name, filterBy::class.simpleName, sourceItem)
                return ProcessingContent(name, itemContent).copy(status = FILTERED)
            }

            processLock.lock {
                itemContent.updateFileStatus(secondaryFileMover, fileExistsDetector)
                val downloadableTargetPaths = itemContent.downloadableFiles().map { it.targetPath() }
                if (log.isDebugEnabled) {
                    val filesStatus = itemContent.downloadableFiles().map {
                        it.targetFilename to it.status
                    }
                    log.debug("Processor:'{}' item:{} file status:{}", name, sourceItem.title, filesStatus)
                }
                // 在处理完后释放
                secondaryFileMover.preoccupiedTargetPath(downloadableTargetPaths)
            }

            val replaceFiles = identifyFilesToReplace(itemContent)
            val (shouldDownload, contentStatus) = probeContentStatus(itemContent, replaceFiles)
            log.trace(
                "Processor:'{}' item:{}, shouldDownload: {}, contentStatus:{}",
                name, sourceItem, shouldDownload, contentStatus
            )
            val processingContent = postProcessingContent(
                ProcessingContent(name, itemContent).copy(status = contentStatus)
            )

            if (shouldDownload) {
                val success = doDownload(processingContent, replaceFiles)
                if (success && downloader !is AsyncDownloader) {
                    log.trace("Processor:'{}' start rename item:{}", name, sourceItem)
                    val moveSuccess = moveFiles(itemContent)
                    val replaceSuccess = replaceFiles(itemContent)
                    if (moveSuccess || replaceSuccess) {
                        return processingContent.copy(status = RENAMED, renameTimes = 1)
                    }
                    return processingContent.copy(status = FAILURE)
                }
            }

            return processingContent
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
            log.info(
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
            processItems()
        }
    }

    fun invokeListeners(
        mode: ListenerMode = ListenerMode.EACH,
        inProcess: Boolean = true,
        block: ProcessListener.() -> Unit
    ) {
        if (inProcess && downloader is AsyncDownloader) {
            log.trace("Processor:'{}' async downloader not invoke listeners in process", name)
            return
        }
        if (mode != options.listenerMode) {
            log.trace("Processor:'{}' listener mode:{} not match", name, mode)
            return
        }
        log.trace("Processor:'{}' invoke listeners", name)
        for (listener in processListeners) {
            listener.runCatching {
                block.invoke(this)
            }.onFailure {
                log.error("${listener::class.simpleName}发生错误", it)
            }
        }
    }

    private inner class NormalProcess : Process() {

        override fun onProcessCompleted(processContext: ProcessContext) {
            invokeListeners(ListenerMode.BATCH) {
                this.onProcessCompleted(processContext)
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
                if (options.recordMinimized && processingContent.status == FILTERED) {
                    return
                }
                processingStorage.save(processingContent)
            }

            val warningFiles = processingContent.itemContent.sourceFiles.filter { it.status.isWarning() }
            if (warningFiles.isNotEmpty()) {
                log.warn(
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
                this.onItemSuccess(processingContent.itemContent)
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
            invokeListeners(ListenerMode.EACH) {
                this.onItemError(item, throwable)
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
                val currP = ProcessorSourceState.resolvePointer(source::class, currentSourceState.lastPointer.values)
                if (currP != lastP) {
                    log.info(
                        "Processor:'$name' update pointer:${currentSourceState.formatPointerString()}"
                    )
                }
                val save = processingStorage.save(currentSourceState)
                sourceState.id = save.id
            }
        }
    }

    private inner class DryRunProcess(
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
            return processingContent.copy(id = content.id, processorName = name)
        }

        override fun onItemCompleted(processingContent: ProcessingContent) {
            if (options.saveProcessingContent) {
                processingStorage.save(processingContent)
            }
        }

        override fun selectItemFilters(): List<SourceItemFilter> {
            return super.selectItemFilters().filter { it !is SourceHashingItemFilter }
        }
    }

}

val log: Logger = LoggerFactory.getLogger(SourceProcessor::class.java)

private class ProcessStat(
    private val name: String,
    var processingCounting: Int = 0,
    var filterCounting: Int = 0,
) {

    val stopWatch = StopWatch(name)

    fun incProcessingCounting() {
        processingCounting = processingCounting.inc()
    }

    fun incFilterCounting() {
        filterCounting = filterCounting.inc()
    }

    fun hasChange(): Boolean {
        return processingCounting > 0 || filterCounting > 0
    }

    override fun toString(): String {
        val sb = StringBuilder("'$name' 处理了${processingCounting}个 过滤了${filterCounting}个")
        for (task in stopWatch.taskInfo) {
            sb.append("; [").append(task.taskName).append("] took ").append(task.timeMillis).append(" ms")
            val percent = Math.round(100.0 * task.timeMillis / stopWatch.totalTimeMillis)
            sb.append(" = ").append(percent).append('%')
        }
        return sb.toString()
    }
}