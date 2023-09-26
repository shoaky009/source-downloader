package io.github.shoaky.sourcedownloader.integration

import io.github.shoaky.sourcedownloader.component.source.FixedSource
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.io.path.*
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("integration-test")
@OptIn(ExperimentalPathApi::class)
class SourceProcessorTest : InitializingBean {

    @Autowired
    lateinit var processingStorage: ProcessingStorage

    @Autowired
    lateinit var processorManager: ProcessorManager

    @Test
    fun normal() {
        val selfPath = savePath.resolve("NormalCase")

        val processor = processorManager.getProcessor("NormalCase").get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.query(ProcessingQuery("NormalCase"))
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
        assertEquals(0, processingStorage.query(ProcessingQuery("NormalCaseCopy")).size)
    }

    @Test
    fun given_mixed_status_files() {
        val selfPath = savePath.resolve("FileStatusCase")

        val downloadedFile = downloadPath.resolve("test2.jpg").createIfNotExists()
        val targetFile = selfPath.resolve("test-dir").resolve("test3.jpg").createIfNotExists()
        val processor = processorManager.getProcessor("FileStatusCase").get()

        processor.run()
        val records = processingStorage.query(ProcessingQuery("FileStatusCase")).sortedBy { it.id }
            .associateBy { it.itemContent.sourceItem.title }

        downloadedFile.deleteIfExists()
        targetFile.deleteIfExists()
        assertEquals(FileContentStatus.NORMAL, records.getValue("test1").itemContent.sourceFiles.first().status)
        assertEquals(FileContentStatus.NORMAL, records.getValue("test2").itemContent.sourceFiles.first().status)
        val sourceFile = records.getValue("test-dir").itemContent.sourceFiles.first { it.fileDownloadPath.name == "test3.jpg" }
        assertEquals(FileContentStatus.TARGET_EXISTS, sourceFile.status)
    }

    @Test
    fun given_conflict_status_files() {
        val processor = processorManager.getProcessor("FileStatusCase2").get()

        processor.run()
        val records = processingStorage.query(ProcessingQuery("FileStatusCase2")).sortedBy { it.id }
        val record = records.first { it.itemContent.sourceItem.title == "test-dir" }
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.sourceFiles[0].status)
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.sourceFiles[1].status)
    }

    @Test
    fun given_tagged_files() {
        val selfPath = savePath.resolve("Tagged")

        val processor = processorManager.getProcessor("Tagged").get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.query(ProcessingQuery("Tagged"))
            .associateBy { it.itemContent.sourceItem.title }
        assertEquals(3, contents.size)

        assert(selfPath.resolve(Path("test1", "test1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "2022-01-01", "test2 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test3 - 1.jpg")).exists())
        assert(selfPath.resolve(Path("test-dir", "2022-01-01", "test4 - 2.jpg")).exists())
    }

    @Test
    fun file_grouping_sequence_variable() {
        val processorName = "FileGroupingSeqVariableCase"
        val processor = processorManager.getProcessor(processorName).get()

        processor.run()
        processor.runRename()
        val contents = processingStorage.query(ProcessingQuery(processorName))
            .associateBy { it.itemContent.sourceItem.title }

        val selfPath = savePath.resolve(processorName)
        assertEquals(3, contents.size)

        assert(selfPath.resolve(Path("test1", "1.jpg")).exists())
        assert(selfPath.resolve(Path("test2", "1.jpg")).exists())
        contents.getValue("test-dir").itemContent.sourceFiles.forEach {
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
        val contents = processingStorage.query(ProcessingQuery(processorName))
            .associateBy { it.sourceHash }
        println(Jackson.toJsonString(contents))
        // 如果实现了对被替换文件的状态更新，这里需要断言REPLACED
        assertEquals(FileContentStatus.NORMAL, contents.getValue("a8d643ef958afca3ac59d5193e085381")
            .itemContent.sourceFiles.first().status)
        assertEquals(FileContentStatus.REPLACE, contents.getValue("ca26c76c94a3b2d8886143317fcf7b26")
            .itemContent.sourceFiles.first().status)
    }

    @Test
    fun same_processing_sync_replace_file() {
        val processorName = "SyncReplaceFileCase"
        val processor = processorManager.getProcessor(processorName).get()

        processor.run()
        val contents = processingStorage.query(ProcessingQuery(processorName))
            .associateBy { it.sourceHash }
        println(Jackson.toJsonString(contents))
        // 如果实现了对被替换文件的状态更新，这里需要断言REPLACED
        assertEquals(FileContentStatus.NORMAL, contents.getValue("a8d643ef958afca3ac59d5193e085381")
            .itemContent.sourceFiles.first().status)
        assertEquals(FileContentStatus.REPLACE, contents.getValue("ca26c76c94a3b2d8886143317fcf7b26")
            .itemContent.sourceFiles.first().status)
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

    // 待测试场景
    // processing_record中的status
    // pointer存储
    // saveContent option测试
    // variableConflictStrategy option测试
    // variableNameReplace option测试
    // replaceVariable option测试
    // error_continue
    // listener invoke测试
    // exists 和 replace冲突测试
    companion object {

        private val savePath = testResourcePath.resolve("target")
        private val downloadPath = testResourcePath.resolve("downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            savePath.deleteRecursively()
            downloadPath.deleteRecursively()
        }
    }

    override fun afterPropertiesSet() {
//        componentManager.registerSupplier()
    }
}