package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class ExpressionFileFilterTest {
    @Test
    fun test_simple_exclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            listOf("#filename == '1.txt'")
        )
        assertEquals(false, filter.test(Path("1.txt")))
        assertEquals(true, filter.test(Path("2.txt")))
    }

    @Test
    fun test_simple_inclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            inclusions = listOf("#filename == '1.txt'")
        )
        assertEquals(true, filter.test(Path("1.txt")))
        assertEquals(false, filter.test(Path("2.txt")))
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
        assertEquals(true,
            filter.test(Path("src/test/kotlin/xyz/shoaky/sourcedownloader/core/component/ExpressionFileFilterTest.kt")
            )
        )
    }
}