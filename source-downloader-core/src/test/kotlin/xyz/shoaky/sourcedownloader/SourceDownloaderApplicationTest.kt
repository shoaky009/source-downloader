package xyz.shoaky.sourcedownloader

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import xyz.shoaky.sourcedownloader.core.ProcessingStorage
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Source
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

@SpringBootTest
@ActiveProfiles("integration-test")
class SourceDownloaderApplicationTest {

    @Autowired
    lateinit var processingStorage: ProcessingStorage

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @BeforeEach
    fun initSourceFiles() {
        val path = Path("src/test/resources/sources/test1.jpg")
        if (path.notExists()) {
            path.createFile()
        }
        val path2 = Path("src/test/resources/sources/test2.jpg")
        if (path2.notExists()) {
            path2.createFile()
        }

        Path("src/test/resources/downloads/test1.jpg").deleteIfExists()
        Path("src/test/resources/downloads/test2.jpg").deleteIfExists()
    }

    @Test
    fun normal() {
        println(processingStorage)

        // Thread.sleep(10000L)
    }
}

object TestSource : Source {
    override fun fetch(): List<SourceItem> {
        return listOf(
            sourceItem("test1.jpg", "image/jpg", "file:src/test/resources/sources/test1.jpg", downloadUrl = "file:src/test/resources/sources/test1.jpg"),
            sourceItem("test2.jpg", "image/jpg", "file:src/test/resources/sources/test2.jpg", downloadUrl = "file:src/test/resources/sources/test2.jpg"),
        )
    }

}

@Component
class TestSourceSupplier : SdComponentSupplier<TestSource> {
    override fun apply(props: ComponentProps): TestSource {
        return TestSource
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.source("test1"))
    }

    override fun getComponentClass(): Class<TestSource> {
        return TestSource::class.java
    }
}