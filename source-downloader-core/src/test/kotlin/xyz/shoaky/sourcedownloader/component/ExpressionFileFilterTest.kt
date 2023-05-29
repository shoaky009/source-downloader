package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.ExpressionFileFilterSupplier
import xyz.shoaky.sourcedownloader.sdk.TestFileContent
import kotlin.io.path.Path
import kotlin.test.assertEquals

class ExpressionFileFilterTest {
    @Test
    fun test_simple_exclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            listOf("#filename == '1.txt'")
        )
        val testFileContent1 = TestFileContent(Path("1.txt"))
        assertEquals(false, filter.test(testFileContent1))

        val testFileContent2 = TestFileContent(Path("2.txt"))
        assertEquals(true, filter.test(testFileContent2))
    }

    @Test
    fun test_simple_inclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            inclusions = listOf("#filename == '1.txt'")
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
                "#size > &'1MB'",
                "#filename matches '.*qaz.*'"
            ),
            inclusions = listOf(
                "#size < &'1MB'",
                "#filename matches '.*Test.*'"
            ),
        )

        val path = Path("src", "test", "kotlin", "xyz", "shoaky",
            "sourcedownloader", "core", "component", "ExpressionFileFilterTest.kt")

        assertEquals(true, filter.test(TestFileContent(path)))
    }
}