package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.ItemDirectoryExistsDetectorSupplier
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.processor.RenamerTest.Companion.downloadPath
import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.io.path.Path

class ItemDirectoryExistsDetectorTest {

    private val detector = ItemDirectoryExistsDetectorSupplier.apply(Properties.EMPTY)

    @Test
    fun exists() {
        val fileMover = Mockito.mock(FileMover::class.java)
        Mockito.`when`(fileMover.exists(
            listOf(Path("save", "test").toAbsolutePath())
        )).thenReturn(true)
        val content = FixedItemContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    Path("test", "test1.mp4").toAbsolutePath(),
                    Path("save").toAbsolutePath(),
                    downloadPath.toAbsolutePath(),
                    MapPatternVariables(),
                    CorePathPattern("test"),
                    CorePathPattern.ORIGIN,
                    Path("save", "test").toAbsolutePath(),
                    "test1.mp4"
                )
            )
        )
        val exists = detector.exists(fileMover, content)
        assert(exists)
    }

    @Test
    fun not_exists() {
        val fileMover = Mockito.mock(FileMover::class.java)
        Mockito.`when`(fileMover.exists(
            listOf(Path("save", "test").toAbsolutePath())
        )).thenReturn(false)
        val content = FixedItemContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    Path("test", "test1.mp4").toAbsolutePath(),
                    Path("save").toAbsolutePath(),
                    downloadPath.toAbsolutePath(),
                    MapPatternVariables(),
                    CorePathPattern("test"),
                    CorePathPattern.ORIGIN,
                    Path("save", "test").toAbsolutePath(),
                    "test1.mp4"
                )
            )
        )
        val exists = detector.exists(fileMover, content)
        assert(exists.not())
    }
}