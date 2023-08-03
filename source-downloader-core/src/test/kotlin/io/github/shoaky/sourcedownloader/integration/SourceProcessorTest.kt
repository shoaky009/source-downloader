package io.github.shoaky.sourcedownloader.integration

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.io.path.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

        val processor = processorManager.getProcessor("NormalCase")?.get()
        assertNotNull(processor, "Processor NormalCase not found")

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
        val processor = processorManager.getProcessor("NormalCaseCopy")?.get()
        assertNotNull(processor, "Processor NormalCaseCopy not found")
        val contents = processor.dryRun()
        assertEquals(3, contents.size)
        assertEquals(0, processingStorage.query(ProcessingQuery("NormalCaseCopy")).size)
    }

    @Test
    fun given_mixed_status_files() {
        val selfPath = savePath.resolve("FileStatusCase")

        val downloadedFile = downloadPath.resolve("test2.jpg").createIfNotExists()
        val targetFile = selfPath.resolve("test-dir").resolve("test3.jpg").createIfNotExists()
        val processor = processorManager.getProcessor("FileStatusCase")?.get()
        assertNotNull(processor, "Processor FileStatusCase not found")

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
        val processor = processorManager.getProcessor("FileStatusCase2")?.get()
        assertNotNull(processor, "Processor FileStatusCase2 not found")

        processor.run()
        val records = processingStorage.query(ProcessingQuery("FileStatusCase2")).sortedBy { it.id }
        val record = records.first { it.itemContent.sourceItem.title == "test-dir" }
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.sourceFiles[0].status)
        assertEquals(FileContentStatus.FILE_CONFLICT, record.itemContent.sourceFiles[1].status)
    }

    @Test
    fun given_tagged_files() {
        val selfPath = savePath.resolve("Tagged")

        val processor = processorManager.getProcessor("Tagged")?.get()
        assertNotNull(processor, "Processor NormalCase not found")

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

    // 待测试场景
    // processing_record中的status
    // pointer存储
    // saveContent option测试
    // variableConflictStrategy option测试
    // variableNameReplace option测试
    // replaceVariable option测试
    // fileReplacement 测试
    // error_continue
    companion object {

        private val savePath = testResourcePath.resolve("target")
        private val downloadPath = testResourcePath.resolve("downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            savePath.deleteRecursively()
            downloadPath.deleteRecursively()
            Path("test.db").deleteIfExists()
        }
    }

    override fun afterPropertiesSet() {
//        componentManager.registerSupplier()
    }
}