package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceFileContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

class GeneralFileMoverTest {

    private val mover = GeneralFileMoverSupplier.apply(ComponentProps.fromMap(emptyMap()))

    private val testFilePaths = listOf(
        Path("src/test/resources/downloads/1.txt"),
        Path("src/test/resources/downloads/2.txt")
    )

    @BeforeEach
    fun setUp() {
        testFilePaths
            .filter { it.notExists() }
            .forEach {
                Files.createFile(it)
            }
        Path("src/test/resources/target/1.txt").deleteIfExists()
        Path("src/test/resources/target/2.txt").deleteIfExists()
    }

    @Test
    fun rename() {
        val file1 = SourceFileContent(
            Path("src/test/resources/downloads/1.txt"),
            Path("src/test/resources/target"),
            MapPatternVariables(),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )
        val file2 = SourceFileContent(
            Path("src/test/resources/downloads/2.txt"),
            Path("src/test/resources/target"),
            MapPatternVariables(),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )

        val sourceContent = SourceContent(sourceItem(), listOf(file1, file2))
        val result = mover.rename(sourceContent)
        assert(result)
        Files.deleteIfExists(file1.targetPath())
        Files.deleteIfExists(file2.targetPath())
    }

}