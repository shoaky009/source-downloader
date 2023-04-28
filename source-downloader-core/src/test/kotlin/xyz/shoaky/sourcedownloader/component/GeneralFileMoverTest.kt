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
    private val savePath = Path("src", "test", "resources", "target")
    private val downloadPath = Path("src", "test", "resources", "downloads")

    private val testFilePaths = listOf(
        downloadPath.resolve("1.txt"),
        downloadPath.resolve("2.txt"),
    )

    @BeforeEach
    fun setUp() {
        testFilePaths
            .forEach {
                it.createIfNotExists()
            }

        savePath.resolve("1.txt").deleteIfExists()
        savePath.resolve("2.txt").deleteIfExists()
        savePath.createDirectories()
    }

    @Test
    fun rename() {
        val file1 = CoreFileContent(
            downloadPath.resolve("1.txt"),
            savePath,
            downloadPath,
            MapPatternVariables(),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )
        val file2 = CoreFileContent(
            downloadPath.resolve("2.txt"),
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