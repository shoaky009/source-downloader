package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.ExpressionItemFilterSupplier
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
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
            listOf("item.title == 'test1234'")
        )
        assertEquals(false, filter.test(sourceItem))
        assertEquals(true, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_simple_inclusions() {
        val sourceItem = sourceItem("test1234")
        val filter = ExpressionItemFilterSupplier.expressions(
            inclusions = listOf("item.title == 'test1234'")
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_multiple() {
        val sourceItem = sourceItem("222Test111")
        val filter = ExpressionItemFilterSupplier.expressions(
            listOf(
                "item.datetime > timestamp('2130-03-31T00:00:00Z')",
                "item.title.matches('qaz')"
            ),
            listOf(
                "item.datetime > timestamp('2023-03-30T00:00:00Z') && item.datetime < timestamp('2130-03-31T00:00:00Z')",
                "item.title.matches('Test')"
            ),
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("1wsaqazfff")))
    }

    @Test
    fun test_regex() {
        val sourceItem = sourceItem("[Nekomoe kissaten][The iDOLM@STER CINDERELLA GIRLS U149][02][720p][JPSC]")
        val filter = ExpressionItemFilterSupplier.expressions(
            exclusions = listOf(
                "item.title.matches('720(?i)P')"
            ),
        )
        assertEquals(false, filter.test(sourceItem))
    }

}

fun createFileContent(
    fileDownloadPath: Path = Path(""),
    sourceSavePath: Path = Path(""),
    downloadPath: Path = Path(""),
    patternVariables: MapPatternVariables = MapPatternVariables(),
    pathPattern: CorePathPattern = CorePathPattern.origin,
    sourcePathPattern: CorePathPattern = CorePathPattern.origin,
    targetSavePath: Path = Path(""),
    targetFilename: String = "",
    tags: MutableSet<String> = mutableSetOf()
): CoreFileContent {
    return CoreFileContent(
        fileDownloadPath,
        sourceSavePath,
        downloadPath,
        patternVariables,
        pathPattern,
        sourcePathPattern,
        targetSavePath,
        targetFilename,
        tags = tags,
    )
}
