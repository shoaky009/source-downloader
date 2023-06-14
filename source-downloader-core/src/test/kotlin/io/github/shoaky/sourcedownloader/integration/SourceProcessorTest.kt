package io.github.shoaky.sourcedownloader.integration

import io.github.shoaky.sourcedownloader.core.ProcessorManager
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.repo.jpa.ProcessingRecordRepository
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.io.path.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Disabled
@ActiveProfiles("integration-test")
@OptIn(ExperimentalPathApi::class)
class SourceProcessorTest {

    @Autowired
    lateinit var processingStorage: ProcessingRecordRepository

    @Autowired
    lateinit var processorManager: ProcessorManager

    init {
        Path("test.db").deleteIfExists()
        sourcePath.createDirectories()
        savePath.createDirectories()
    }

    @BeforeEach
    fun initFileSource() {
        savePath.deleteRecursively()

        sourcePath.resolve("test1.jpg").createIfNotExists()
        sourcePath.resolve("test2.jpg").createIfNotExists()

        val source3 = sourcePath.resolve("test-dir")
        source3.createDirectories()
        source3.resolve("test3.jpg").createIfNotExists()
        source3.resolve("test4.jpg").createIfNotExists()

        savePath.resolve("test1.jpg").deleteIfExists()
        savePath.resolve("test2.jpg").deleteIfExists()
    }

    @Test
    fun normal() {
        val processor = processorManager.getProcessor("NormalCase")
        assertNotNull(processor, "Processor NormalCase not found")

        processor.run()
        processor.runRename()

        val contents = processingStorage.findByProcessorName("NormalCase")
        assertEquals(3, contents.size)

        val d1 = contents[0].sourceContent.sourceFiles.first().patternVariables.getVariables()["sourceItemDate"]!!
        val d2 = contents[1].sourceContent.sourceFiles.first().patternVariables.getVariables()["sourceItemDate"]!!
        val d3 = contents[2].sourceContent.sourceFiles.first().patternVariables.getVariables()["sourceItemDate"]!!

        assert(savePath.resolve(Path("test1", d1, "test1 - 1.jpg")).exists())
        assert(savePath.resolve(Path("test2", d2, "test2 - 1.jpg")).exists())
        assert(savePath.resolve(Path("test-dir", d3, "test3 - 1.jpg")).exists())
        assert(savePath.resolve(Path("test-dir", d3, "test4 - 2.jpg")).exists())

    }

    @Test
    fun normal_dry_run() {
        val processor = processorManager.getProcessor("NormalCaseCopy")
        assertNotNull(processor, "Processor NormalCaseCopy not found")

        val contents = processor.dryRun()
        assertEquals(3, contents.size)
        assertEquals(0, processingStorage.findByProcessorName("NormalCaseCopy").size)
    }

    @Test
    fun test_file_status() {
        val downloadedFile = downloadPath.resolve("test2.jpg").createIfNotExists()

        val targetFile = savePath.resolve("test-dir")
            .resolve("test3.jpg").createIfNotExists()

        val processor = processorManager.getProcessor("FileStatusCase")
        assertNotNull(processor, "Processor FileStatusCase not found")

        processor.run()
        val records = processingStorage.findByProcessorName("FileStatusCase").sortedBy { it.id }

        downloadedFile.deleteIfExists()
        targetFile.deleteIfExists()
        assertEquals(FileContentStatus.NORMAL, records[0].sourceContent.sourceFiles.first().status)
        assertEquals(FileContentStatus.NORMAL, records[1].sourceContent.sourceFiles.first().status)
        assertEquals(FileContentStatus.TARGET_EXISTS, records[2].sourceContent.sourceFiles[0].status)
    }

    @Test
    fun test_file_status2() {
        val processor = processorManager.getProcessor("FileStatusCase2")
        assertNotNull(processor, "Processor FileStatusCase2 not found")

        processor.run()
        val records = processingStorage.findByProcessorName("FileStatusCase2").sortedBy { it.id }
        assertEquals(FileContentStatus.FILE_CONFLICT, records[2].sourceContent.sourceFiles[0].status)
        assertEquals(FileContentStatus.FILE_CONFLICT, records[2].sourceContent.sourceFiles[1].status)
    }

    // 待测试场景
    // 1.processing_record中的status
    // 2.pointer存储
    // 3.不同tag文件的pattern
    // 4.saveContent option测试
    // 5.variableConflictStrategy option测试
    // 6.variableNameReplace option测试
    // 7.tagFilenamePattern option测试
    // 8.replaceVariable option测试

    companion object {

        private val savePath = testResourcePath.resolve("target")
        private val sourcePath = testResourcePath.resolve("sources")
        private val downloadPath = testResourcePath.resolve("downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            sourcePath.deleteRecursively()
            savePath.deleteRecursively()
            downloadPath.deleteRecursively()
            Path("test.db").deleteIfExists()
        }
    }
}