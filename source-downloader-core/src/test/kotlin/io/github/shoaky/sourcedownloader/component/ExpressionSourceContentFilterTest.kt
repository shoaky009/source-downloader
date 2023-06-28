package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.core.file.CoreSourceContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExpressionSourceContentFilterTest {

    @Test
    fun content_exclusions() {
        val filter = ExpressionSourceContentFileter(exclusions = listOf(
            "files.filter(x, 'video' in x.tags).size() < 3"
        ))

        val sc1 = CoreSourceContent(
            sourceItem(),
            listOf(
                createFileContent(tags = mutableSetOf("video")),
                createFileContent(tags = mutableSetOf("video")),
            ),
            MapPatternVariables()
        )
        assertEquals(false, filter.test(sc1))

        val sc2 = CoreSourceContent(
            sourceItem(),
            listOf(
                createFileContent(tags = mutableSetOf("video")),
                createFileContent(tags = mutableSetOf("video")),
                createFileContent(tags = mutableSetOf("video")),
                createFileContent(tags = mutableSetOf("video")),
                createFileContent(tags = mutableSetOf("video")),
            ),
            MapPatternVariables()
        )
        assertEquals(true, filter.test(sc2))
    }

    @Test
    fun content_inclusions() {
        val filter = ExpressionSourceContentFileter(inclusions = listOf(
            "files.filter(x, 'video' in x.tags).size() == 1"
        ))

        val sc1 = CoreSourceContent(
            sourceItem(),
            listOf(
                createFileContent(tags = mutableSetOf("image")),
                createFileContent(tags = mutableSetOf("image")),
            ),
            MapPatternVariables()
        )
        assertEquals(false, filter.test(sc1))

        val sc2 = CoreSourceContent(
            sourceItem(),
            listOf(
                createFileContent(tags = mutableSetOf("image")),
                createFileContent(tags = mutableSetOf("video")),
            ),
            MapPatternVariables()
        )
        assertEquals(true, filter.test(sc2))
    }
}