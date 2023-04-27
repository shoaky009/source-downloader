package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.GeneralFileMoverSupplier
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.createIfNotExists
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

class GeneralFileMoverTest {

    private val mover = GeneralFileMoverSupplier.apply(Properties.fromMap(emptyMap()))
    private val savePath = Path("src/test/resources/target")
    private val downloadPath = Path("src/test/resources/downloads")

    private val testFilePaths = listOf(
        Path("src/test/resources/downloads/1.txt"),
        Path("src/test/resources/downloads/2.txt")
    )

    @BeforeEach
    fun setUp() {
        testFilePaths
            .forEach {
                it.createIfNotExists()
            }
        Path("src/test/resources/target/1.txt").deleteIfExists()
        Path("src/test/resources/target/2.txt").deleteIfExists()
        savePath.createDirectories()
    }

    @Test
    fun rename() {
        val file1 = CoreFileContent(
            Path("src/test/resources/downloads/1.txt"),
            savePath,
            downloadPath,
            MapPatternVariables(),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )
        val file2 = CoreFileContent(
            Path("src/test/resources/downloads/2.txt"),
            savePath,
            downloadPath,
            MapPatternVariables(),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )

        val sourceContent = PersistentSourceContent(sourceItem(), listOf(file1, file2), MapPatternVariables())
        val result = mover.rename(sourceContent)
        assert(result)
        Files.deleteIfExists(file1.targetPath())
        Files.deleteIfExists(file2.targetPath())
    }

}