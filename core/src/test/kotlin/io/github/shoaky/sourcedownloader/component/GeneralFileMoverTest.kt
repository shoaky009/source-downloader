package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.GeneralFileMoverSupplier
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.RenameVariables
import io.github.shoaky.sourcedownloader.core.file.Renamer
import io.github.shoaky.sourcedownloader.core.processor.createRawFileContent
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
class GeneralFileMoverTest {

    private val mover = GeneralFileMoverSupplier.apply(
        CoreContext.empty, Properties.fromMap(emptyMap())
    )
    private val testFilePaths = listOf(
        downloadPath.resolve("1.txt"),
        downloadPath.resolve("2.txt"),
    )

    @BeforeEach
    fun setUp() {
        downloadPath.createDirectories()
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
        val file1 = Renamer().createFileContent(
            sourceItem(),
            createRawFileContent(
                Path("1.txt"),
                savePath,
                downloadPath,
                MapPatternVariables(),
                CorePathPattern.origin,
                CorePathPattern.origin,
            ), RenameVariables.EMPTY
        )
        val file2 = Renamer().createFileContent(
            sourceItem(),
            createRawFileContent(
                Path("2.txt"),
                savePath,
                downloadPath,
                MapPatternVariables(),
                CorePathPattern.origin,
                CorePathPattern.origin,
            ), RenameVariables.EMPTY
        )
        val result1 = mover.move(sourceItem(), file1)
        val result2 = mover.move(sourceItem(), file2)
        assert(result1)
        assert(result2)
        Files.deleteIfExists(file1.targetPath())
        Files.deleteIfExists(file2.targetPath())
    }

    companion object {

        private val savePath = Path("src", "test", "resources", "target")
        private val downloadPath = Path("src", "test", "resources", "downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            savePath.deleteRecursively()
            downloadPath.deleteRecursively()
        }
    }

}