package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfig
import java.nio.file.Files
import java.time.Duration
import kotlin.io.path.Path
import kotlin.test.assertEquals

class YamlConfigStorageTest {
    private val path = Path("src/test/resources/config-test2.yaml")
    private val storage = YamlConfigStorage(path)

    @BeforeEach
    fun restore() {
        Files.writeString(path, """
components:
  source:
    - name: "test"
      type: "test"
processors:
  - name: "test-normal-case"
    triggers:
      - "test"
    source: "test"
    downloader: "test"
    variable-providers:
      - "test"
    file-mover: "test"
    save-path: "test-path"
    options:
      rename-task-interval: "PT1M40S"
        """.trimIndent())
    }

    @Test
    fun read() {
        val config = storage.getAllComponentConfig()
        assert(config.isNotEmpty())
        val first = config["source"]?.first()
        assertEquals("test", first?.name)
    }

    @Test
    fun write_component() {
        storage.save("source",
            ComponentConfig(
                "test",
                "test1"
            ))
        val config = storage.getAllComponentConfig()["source"]?.first()
        assertEquals("test1", config?.type)
    }

    @Test
    fun write_processor() {
        storage.save("test-normal-case", ProcessorConfig(
            "test-normal-case",
            listOf(ProcessorConfig.ComponentId("test")),
            ProcessorConfig.ComponentId("test"),
            listOf(ProcessorConfig.ComponentId("test")),
            ProcessorConfig.ComponentId("test"),
            ProcessorConfig.ComponentId("test"),
            Path(""),
            options = ProcessorConfig.Options(
                renameTaskInterval = Duration.ofSeconds(100L)
            )
        ))

        val processor = storage.getAllProcessorConfig().first()
        assertEquals(Duration.ofSeconds(100L), processor.options.renameTaskInterval)
    }
}