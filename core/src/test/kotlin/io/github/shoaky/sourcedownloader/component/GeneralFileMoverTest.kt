package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.GeneralFileMoverSupplier
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.core.processor.Renamer
import io.github.shoaky.sourcedownloader.core.processor.createRawFileContent
import io.github.shoaky.sourcedownloader.createIfNotExists
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
class GeneralFileMoverTest {

    private val mover = GeneralFileMoverSupplier.apply(Properties.fromMap(emptyMap()))
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
        val file1 = Renamer().createFileContent(sourceItem(), createRawFileContent(
            downloadPath.resolve("1.txt"),
            savePath,
            downloadPath,
            MapPatternVariables(),
            CorePathPattern.ORIGIN,
            CorePathPattern.ORIGIN,
        ), PatternVariables.EMPTY)
        val file2 = Renamer().createFileContent(sourceItem(), createRawFileContent(
            downloadPath.resolve("2.txt"),
            savePath,
            downloadPath,
            MapPatternVariables(),
            CorePathPattern.ORIGIN,
            CorePathPattern.ORIGIN,
        ), PatternVariables.EMPTY)
        val itemContent = CoreItemContent(sourceItem(), listOf(file1, file2), MapPatternVariables())
        val result = mover.move(itemContent)
        assert(result)
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