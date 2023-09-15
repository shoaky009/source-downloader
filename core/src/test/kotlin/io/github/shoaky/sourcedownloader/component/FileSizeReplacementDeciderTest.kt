package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.FixedFileContent
import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class FileSizeReplacementDeciderTest {

    @Test
    fun test() {
        val decider = FileSizeReplacementDecider
        val content1 = FixedItemContent(
            sourceItem(),
            listOf(
                FixedFileContent(Path(""), attrs = mapOf("size" to 1024))
            )
        )

        assertEquals(false, decider.isReplace(content1, null, SourceFile(Path(""), attrs = mapOf("size" to 1024))))
        assertEquals(true, decider.isReplace(content1, null, SourceFile(Path(""), attrs = mapOf("size" to 1023))))
    }
}