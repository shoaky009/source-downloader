package xyz.shoaky.sourcedownloader

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import xyz.shoaky.sourcedownloader.core.ProcessorManager
import xyz.shoaky.sourcedownloader.repo.jpa.ProcessingRecordRepository
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.*
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("integration-test")
@OptIn(ExperimentalPathApi::class)
class SourceProcessorApplicationTest {

    @Autowired
    lateinit var processingStorage: ProcessingRecordRepository

    @Autowired
    lateinit var processorManager: ProcessorManager
    val savePath = Path("src/test/resources/target")

    @BeforeEach
    fun beforeNormal() {
        savePath.deleteRecursively()
        val sourcePath = Path("src/test/resources/sources/")

        sourcePath.resolve("test1.jpg").createIfNotExists()
        sourcePath.resolve("test2.jpg").createIfNotExists()

        val source3 = sourcePath.resolve("test-dir")
        source3.createDirectories()
        source3.resolve("test3.jpg").createIfNotExists()
        source3.resolve("test4.jpg").createIfNotExists()

        Path("src/test/resources/target/test1.jpg").deleteIfExists()
        Path("src/test/resources/target/test2.jpg").deleteIfExists()
    }

    @Test
    fun normal() {
        val processor = processorManager.getProcessor("NormalCase")
            ?: throw IllegalStateException("Processor not found")
        processor.run()
        processor.runRename()

        val contents = processingStorage.findByProcessorName("NormalCase")
        assertEquals(3, contents.size)

        val now = LocalDate.now()
        assert(Path("src/test/resources/target/test1/$now/test1 - 1.jpg").exists())
        assert(Path("src/test/resources/target/test2/$now/test2 - 1.jpg").exists())
        assert(Path("src/test/resources/target/test-dir/$now/test3 - 1.jpg").exists())
        assert(Path("src/test/resources/target/test-dir/$now/test4 - 2.jpg").exists())

        savePath.deleteRecursively()
    }

    @Test
    fun normal_dry_run() {
        val processor = processorManager.getProcessor("NormalCase-Copy")
            ?: throw IllegalStateException("Processor not found")

        val contents = processor.dryRun()
        assertEquals(3, contents.size)
        assertEquals(0, processingStorage.findByProcessorName("NormalCase-Copy").size)
    }
}

fun Path.createIfNotExists(): Path {
    if (this.exists()) {
        return this
    }
    return Files.createFile(this)
}
