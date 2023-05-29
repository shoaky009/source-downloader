package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.ExpressionFileFilterSupplier
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.TestFileContent
import kotlin.io.path.Path
import kotlin.io.path.fileSize
import kotlin.test.assertEquals

class ExpressionFileFilterTest {
    @Test
    fun test_simple_exclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            listOf("filename == '1.txt'")
        )
        val testFileContent1 = TestFileContent(Path("1.txt"))
        assertEquals(false, filter.test(testFileContent1))

        val testFileContent2 = TestFileContent(Path("2.txt"))
        assertEquals(true, filter.test(testFileContent2))
    }

    @Test
    fun test_simple_inclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            inclusions = listOf("filename == '1.txt'")
        )
        val testFileContent1 = TestFileContent(Path("1.txt"))
        assertEquals(true, filter.test(testFileContent1))

        val testFileContent2 = TestFileContent(Path("2.txt"))
        assertEquals(false, filter.test(testFileContent2))
    }

    @Test
    fun test_multiple() {
        val filter = ExpressionFileFilterSupplier.expressions(
            exclusions = listOf(
                "attr['size'] > 1024*1024",
                "filename.matches('.*qaz.*')"
            ),
            inclusions = listOf(
                "attr['size'] < 1024*1024",
                "filename.matches('.*Test.*')"
            ),
        )

        val path = Path("src", "test", "kotlin", "xyz", "shoaky", "sourcedownloader", "component", "ExpressionFileFilterTest.kt")
        assertEquals(true, filter.test(
            TestFileContent(path, attributes = mapOf("size" to path.fileSize())),
        ))
    }


    @Test
    fun test_all_variables() {
        val filter = ExpressionFileFilter(
            inclusions = listOf("""
                filename.contains('test') && 
                'video' in tags &&
                ext == 'txt' &&
                pv['test'] == 'test' &&
                attr['size'] > 10
            """.trimIndent())
        )

        val testFileContent = TestFileContent(
            fileDownloadPath = Path("test.txt"),
            tags = setOf("video"),
            patternVariables = MapPatternVariables(mapOf("test" to "test")),
            attributes = mapOf("size" to 100)
        )
        assertEquals(true, filter.test(testFileContent))
    }
}