package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.FixedFileContent
import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.test.assertContentEquals

class MediaTypeExistsDetectorTest {

    @Test
    fun exists() {
        val content = FixedItemContent(
            SourceItem("", "http://localhost", LocalDateTime.MIN, "", "http://localhost"),
            listOf(
                FixedFileContent(
                    Path("test", "test1.mp4"),
                )
            )
        )
        val fileMover = Mockito.mock(FileMover::class.java)
        Mockito.`when`(fileMover.listFiles(Path("test", "test1.mp4"))).thenAnswer {
            listOf(Path("test", "test1.mkv"))
        }
        val exists = MediaTypeExistsDetector.exists(
            fileMover, content
        ).values

        assertContentEquals(listOf(Path("test/test1.mkv")), exists)
    }

    @Test
    fun not_exists() {
        val content = FixedItemContent(
            SourceItem("", "http://localhost", LocalDateTime.MIN, "", "http://localhost"),
            listOf(
                FixedFileContent(
                    Path("test", "test2.mp4"),
                )
            )
        )
        val fileMover = Mockito.mock(FileMover::class.java)
        Mockito.`when`(fileMover.listFiles(Path("test", "test2.mp4"))).thenAnswer {
            listOf(
                Path("test", "test1.mkv"),
                Path("test", "test1.mp4")
            )
        }
        val exists = MediaTypeExistsDetector.exists(
            fileMover, content
        ).values

        assertContentEquals(listOf(null), exists)
    }
}