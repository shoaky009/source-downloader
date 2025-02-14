package io.github.shoaky.sourcedownloader.integration

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.component.source.FixedSource
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.integration.support.DelayItemDownloader
import io.github.shoaky.sourcedownloader.integration.support.DelayItemDownloaderSupplier
import io.github.shoaky.sourcedownloader.integration.support.Item2ReplaceDeciderSupplier
import io.github.shoaky.sourcedownloader.integration.support.TestDirErrorDownloaderSupplier
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import io.github.shoaky.sourcedownloader.repo.exposed.ExposedProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.testResourcePath
import io.github.shoaky.sourcedownloader.util.RestorableConfigOperator
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import kotlin.io.path.*
import kotlin.test.assertEquals

@OptIn(ExperimentalPathApi::class)
class SourceProcessorTest {

    private val props = SourceDownloaderProperties(testResourcePath)
    private val container = SimpleObjectWrapperContainer()
    private val processingStorage: ProcessingStorage = ExposedProcessingStorage()
    private val instanceManager = DefaultInstanceManager(configOperator)
    private val componentManager: ComponentManager = DefaultComponentManager(
        container,
        listOf(
            DefaultComponents(),
            configOperator
        )
    )
    private val pluginManager = PluginManager(
        componentManager,
        instanceManager,
        props
    )
    private val processorManager: ProcessorManager =
        DefaultProcessorManager(processingStorage, componentManager, container)

    private val application = CoreApplication(
        props,
        DefaultInstanceManager(configOperator),
        componentManager,
        processorManager,
        pluginManager,
        listOf(configOperator),
        listOf(
            DelayItemDownloaderSupplier,
            Item2ReplaceDeciderSupplier,
            TestDirErrorDownloaderSupplier
        )
    )

    init {
        val dataSource = SingleConnectionDataSource("jdbc:sqlite::memory:", true)
        dataSource.setDriverClassName("org.sqlite.JDBC")
        dataSource.password = "sd"
        dataSource.username = "sd"
        val flyway = Flyway.configure().dataSource(dataSource)
            .locations("classpath:/db/migration")
            .load()
        flyway.migrate()
        Database.connect(dataSource)
        application.start()
    }

    @Test
    fun normal() {
        val selfPath = savePath.resolve("NormalCase")
        val processor = processorManager.getProcessor("NormalCase").get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.queryAllContent(ProcessingQuery("NormalCase"))
            .associateBy { it.itemContent.sourceItem.title }
        assertEquals(3, contents.size)

        assert(selfPath.resolve(Path("test1", "2022-01-01", "test1 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "2022-01-01", "test2 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test3 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test4 - 2.jpg")).exists())

    }

    @Test
    fun normal_dry_run() {
        val processor = processorManager.getProcessor("NormalCaseCopy").get()
        val contents = processor.dryRun()
        assertEquals(3, contents.size)
        assertEquals(0, processingStorage.queryAllContent(ProcessingQuery("NormalCaseCopy")).size)
    }

    @Test
    fun given_mixed_status_files() {
        val selfPath = savePath.resolve("FileStatusCase")

        val downloadedFile = downloadPath.resolve("test2.jpg").createIfNotExists()
        val targetFile = selfPath.resolve("test-dir").resolve("test3.jpg").createIfNotExists()
        val processor = processorManager.getProcessor("FileStatusCase").get()

        processor.run()
        val records = processingStorage.queryAllContent(ProcessingQuery("FileStatusCase")).sortedBy { it.id }
            .associateBy { it.itemContent.sourceItem.title }

        downloadedFile.deleteIfExists()
        targetFile.deleteIfExists()
        assertEquals(FileContentStatus.NORMAL, records.getValue("test1").itemContent.fileContents.first().status)
        assertEquals(FileContentStatus.NORMAL, records.getValue("test2").itemContent.fileContents.first().status)
        val sourceFile =
            records.getValue("test-dir").itemContent.fileContents.first { it.fileDownloadPath.name == "test3.jpg" }
        assertEquals(FileContentStatus.TARGET_EXISTS, sourceFile.status)
    }

    @Test
    fun given_conflict_status_files() {
        val processor = processorManager.getProcessor("FileStatusCase2").get()

        processor.run()
        val records = processingStorage.queryAllContent(ProcessingQuery("FileStatusCase2")).sortedBy { it.id }
        val record = records.first { it.itemContent.sourceItem.title == "test-dir" }
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.fileContents[0].status)
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.fileContents[1].status)
    }

    @Test
    fun given_file_grouping_options() {
        val name = "FileGroupingCase"
        val selfPath = savePath.resolve(name)

        val processor = processorManager.getProcessor(name).get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.queryAllContent(ProcessingQuery(name))
            .associateBy { it.itemContent.sourceItem.title }
        assertEquals(3, contents.size)

        assert(selfPath.resolve(Path("test1", "test1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "2022-01-01", "test2 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test3 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test4 - 2.jpg")).exists())
    }

    @Test
    fun given_file_grouping_filter_options() {
        val name = "FileGroupingFilterCase"
        val selfPath = savePath.resolve(name)

        val processor = processorManager.getProcessor(name).get()

        processor.run()
        processor.runRename()

        assert(selfPath.resolve(Path("test1", "test1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "test2.jpg")).notExists())
        assert(selfPath.resolve(Path("test-dir", "test3.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "test4.jpg")).notExists())
    }

    @Test
    fun file_grouping_sequence_variable() {
        val processorName = "FileGroupingSeqVariableCase"
        val processor = processorManager.getProcessor(processorName).get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.queryAllContent(ProcessingQuery(processorName))
            .associateBy { it.itemContent.sourceItem.title }

        val selfPath = savePath.resolve(processorName)
        assertEquals(3, contents.size)

        assert(selfPath.resolve(Path("test1", "1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "1.jpg")).exists())
        contents.getValue("test-dir").itemContent.fileContents.forEach {
            assertEquals(FileContentStatus.FILE_CONFLICT, it.status)
            assertEquals("1.jpg", it.targetFilename)
        }
    }

    @Test
    fun same_processing_async_replace_file() {
        val processorName = "AsyncReplaceFileCase"
        val processor = processorManager.getProcessor(processorName).get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.queryAllContent(ProcessingQuery(processorName))
            .associateBy { it.itemContent.sourceItem.title }
        // 如果实现了对被替换文件的状态更新，这里需要断言REPLACED
        assertEquals(
            FileContentStatus.NORMAL, contents.getValue("test-replace1")
                .itemContent.fileContents.first().status
        )
        assertEquals(
            FileContentStatus.REPLACE, contents.getValue("test-replace2")
                .itemContent.fileContents.first().status
        )
    }

    @Test
    fun same_processing_sync_replace_file() {
        val processorName = "SyncReplaceFileCase"
        val processor = processorManager.getProcessor(processorName).get()

        processor.run()
        val contents = processingStorage.queryAllContent(ProcessingQuery(processorName))
            .associateBy { it.itemContent.sourceItem.title }
        // 如果实现了对被替换文件的状态更新，这里需要断言REPLACED
        assertEquals(
            FileContentStatus.NORMAL, contents.getValue("test-replace1")
                .itemContent.fileContents.first().status
        )
        assertEquals(
            FileContentStatus.REPLACE, contents.getValue("test-replace2")
                .itemContent.fileContents.first().status
        )
    }

    @Test
    fun pointer_storage() {
        val processorName = "PointerWriteReadCase"
        val processor = processorManager.getProcessor(processorName).get()
        processor.run()
        var state = processingStorage.findProcessorSourceState(processorName, processor.sourceId)
        assert(state?.lastPointer != null)
        val p1 = state?.lastPointer?.values ?: mutableMapOf()

        val op1 = ProcessorSourceState.resolvePointer(FixedSource::class, p1)
        assertEquals(processor.sourceId, state?.sourceId)
        assertEquals(1, op1.offset)

        processor.run()
        state = processingStorage.findProcessorSourceState(processorName, processor.sourceId)

        val p2 = state?.lastPointer?.values ?: mutableMapOf()
        val op2 = ProcessorSourceState.resolvePointer(FixedSource::class, p2)
        assertEquals(2, op2.offset)
    }

    @Test
    fun media_type() {
        val processorName = "MediaTypeExistCase"
        val processor = processorManager.getProcessor(processorName).get()
        processor.run()
        val contents =
            processingStorage.queryAllContent(ProcessingQuery(processorName))
                .associateBy { it.itemContent.sourceItem.title }
        assertEquals(ProcessingContent.Status.WAITING_TO_RENAME, contents.getValue("test1").status)
        assertEquals(ProcessingContent.Status.TARGET_ALREADY_EXISTS, contents.getValue("test2").status)
    }

    @Test
    fun pattern_order() {
        val processorName = "PatternOrderCase"
        val processor = processorManager.getProcessor(processorName).get()
        val contents = processor.dryRun()
            .associateBy { it.itemContent.sourceItem.title }
        val item1 = contents.getValue("test1").itemContent.fileContents.first()
        val item2 = contents.getValue("test2").itemContent.fileContents.first()
        val item3 = contents.getValue("test-dir").itemContent.fileContents.first()
        assertEquals("test1_ITEM_GROUPING_1.jpg", item1.targetFilename)
        assertEquals("test2_FILE_GROUPING.jpg", item2.targetFilename)
        assertEquals("test3.jpg", item3.targetFilename)
    }

    @Test
    fun download_error_case() {
        val processorName = "DownloadErrorCase"
        val testItemTitle = "test-dir"
        val processor = processorManager.getProcessor(processorName).get()
        val contents = processor.dryRun().associateBy { it.itemContent.sourceItem.title }
        processor.run()
        val sourceFiles = contents.getValue(testItemTitle).itemContent.fileContents
        val targetPaths = sourceFiles.map { it.targetPath() }
        val anyTargetPaths = processingStorage.targetPathExists(targetPaths).any { it }
        val hasErrorItem = processingStorage.queryAllContent(ProcessingQuery(processorName))
            .any { it.itemContent.sourceItem.title == testItemTitle }
        assert(hasErrorItem.not())
        assert(anyTargetPaths.not())

        processor.run()
        val testItem = processingStorage.queryAllContent(ProcessingQuery("DownloadErrorCase"))
            .first { it.itemContent.sourceItem.title == testItemTitle }

        assertEquals(ProcessingContent.Status.RENAMED, testItem.status)
    }

    @Test
    fun replace_file_cancel_submitted_item() {
        val processorName = "ReplaceFileCancelSubmittedItem"
        val processor = processorManager.getProcessor(processorName).get()
        processor.run()

        val downloader = componentManager.getComponent(
            ComponentTopType.DOWNLOADER,
            ComponentId("delay-item"),
            jacksonTypeRef<ComponentWrapper<DelayItemDownloader>>()
        ).get()
        println(downloader.getCanceled())

        val contents = processingStorage.queryAllContent(ProcessingQuery("ReplaceFileCancelSubmittedItem"))
            .associateBy { it.itemContent.sourceItem.title }
        val test2 = contents.getValue("test2").itemContent.fileContents.first()
        val test1 = contents.getValue("test1").itemContent.fileContents.first()
        val selfPath = savePath.resolve(processorName)
        when (test2.status) {
            FileContentStatus.REPLACE -> {
                assertEquals(FileContentStatus.NORMAL, test1.status)
                assert(selfPath.resolve(Path("1.mkv")).exists())
                assert(selfPath.resolve(Path("1.mp4")).notExists())
            }

            FileContentStatus.NORMAL -> {
                assertEquals(FileContentStatus.REPLACE, test1.status)
                assert(selfPath.resolve(Path("1.mp4")).exists())
                assert(selfPath.resolve(Path("1.mkv")).notExists())
            }

            else -> throw RuntimeException("Should not entry here")
        }

    }

    @Test
    fun parallelism_replace_case() {
        val processorName = "ParallelismReplaceCase"
        val processor = processorManager.getProcessor(processorName).get()
        processor.run()

        val contents = processingStorage.queryAllContent(ProcessingQuery(processorName))
            .associateBy { it.itemContent.sourceItem.title }

        val fileContent1 = contents.getValue("test-replace1").itemContent.fileContents.first()
        val fileContent2 = contents.getValue("test-replace2").itemContent.fileContents.first()
        // test-replace2先完成
        if (fileContent2.status == FileContentStatus.NORMAL) {
            assertEquals(FileContentStatus.TARGET_EXISTS, fileContent1.status)
        }
        // test-replace1先执行
        if (fileContent2.status == FileContentStatus.READY_REPLACE) {
            assertEquals(FileContentStatus.REPLACED, fileContent1.status)
        }
        assert(fileContent2.status in listOf(FileContentStatus.NORMAL, FileContentStatus.READY_REPLACE))
    }

    @Test
    fun too_long_variable_trimming_case() {
        val processorName = "TooLongVariableTrimmingCase"
        val processor = processorManager.getProcessor(processorName).get()
        val file = processor.dryRun().first().itemContent.fileContents.first()
        for (path in processor.savePath.absolute().relativize(file.targetPath())) {
            assert(path.toString().toByteArray().size <= 20)
        }
    }

    // 待测试场景
    // saveContent option测试
    // variableConflictStrategy option测试
    // variableNameReplace option测试
    // replaceVariable option测试
    // error_continue
    // listener invoke测试
    // exists 和 replace冲突测试
    // 并行测试
    // downloadPath文件已存在
    // 文件下载失败重试时
    // Listener调用测试
    companion object {

        private val savePath = testResourcePath.resolve("target")
        private val downloadPath = testResourcePath.resolve("downloads")
        private val dataLocation = testResourcePath
        private val configPath = dataLocation.resolve("config.yaml")
        private val configOperator =
            RestorableConfigOperator(configPath, YamlConfigOperator(configPath).also { it.init() })

        @JvmStatic
        @AfterAll
        fun clean() {
            savePath.deleteRecursively()
            downloadPath.deleteRecursively()
            configOperator.restore()
        }
    }

}