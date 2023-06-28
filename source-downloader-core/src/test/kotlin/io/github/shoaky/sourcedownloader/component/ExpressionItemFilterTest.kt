package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.ExpressionItemFilterSupplier
import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals

class ExpressionItemFilterTest {

    @Test
    fun test_simple_exclusions() {
        val sourceItem = sourceItem("test1234")
        val filter = ExpressionItemFilterSupplier.expressions(
            listOf("title == 'test1234'")
        )
        assertEquals(false, filter.test(sourceItem))
        assertEquals(true, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_simple_inclusions() {
        val sourceItem = sourceItem("test1234")
        val filter = ExpressionItemFilterSupplier.expressions(
            inclusions = listOf("title == 'test1234'")
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_multiple() {
        val sourceItem = sourceItem("222Test111")
        val filter = ExpressionItemFilterSupplier.expressions(
            listOf(
                "date > timestamp('2130-03-31T00:00:00Z')",
                "title.matches('qaz')"
            ),
            listOf(
                "date > timestamp('2023-03-30T00:00:00Z') && date < timestamp('2130-03-31T00:00:00Z')",
                "title.matches('Test')"
            ),
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("1wsaqazfff")))
    }
//

    @Test
    fun test_720p() {
        val sourceItem = sourceItem("[Nekomoe kissaten][The iDOLM@STER CINDERELLA GIRLS U149][02][720p][JPSC]")
        val filter = ExpressionItemFilterSupplier.expressions(
            exclusions = listOf(
                "title.matches('720(?i)P')"
            ),
        )
        assertEquals(false, filter.test(sourceItem))
    }

}

fun createFileContent(
    path: Path = Path(""),
    sourcePath: Path = Path(""),
    sourceRoot: Path = Path(""),
    patternVariables: MapPatternVariables = MapPatternVariables(),
    pathPattern: CorePathPattern = CorePathPattern.ORIGIN,
    sourcePathPattern: CorePathPattern = CorePathPattern.ORIGIN,
    tags: MutableSet<String> = mutableSetOf()
): CoreFileContent {

    return CoreFileContent(
        path,
        sourcePath,
        sourceRoot,
        patternVariables,
        pathPattern,
        sourcePathPattern,
        tags = tags
    )
}
