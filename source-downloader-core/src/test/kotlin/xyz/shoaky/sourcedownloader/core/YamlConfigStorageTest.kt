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
    fun restora() {

        Files.writeString(path, """
components:
  source:
    - name: "test"
      type: "test"
processors:
  - name: "test-normal-case"
    triggers: 
      - "fixed:1s"
    source: "test"
    downloader: "test"
    mover: "test"
    savePath: "test"
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
            emptyList(),
            ProcessorConfig.ComponentId("test"),
            emptyList(),
            ProcessorConfig.ComponentId("test"),
            ProcessorConfig.ComponentId("test"),
            Path(""),
            options = ProcessorConfig.Options(
                renameTaskInterval = Duration.ofSeconds(100L)
            )
        ))
    }
}