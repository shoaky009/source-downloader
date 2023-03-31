package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.ExpressionItemFilterSupplier
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.test.assertEquals

class ExpressionItemFilterTest {


    @Test
    fun test_simple_exclusions() {
        val sourceItem = sourceItem("test1234")
        val filter = ExpressionItemFilterSupplier.expressions(
            listOf("#title == 'test1234'")
        )
        assertEquals(false, filter.test(sourceItem))
        assertEquals(true, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_simple_inclusions() {
        val sourceItem = sourceItem("test1234")
        val filter = ExpressionItemFilterSupplier.expressions(
            inclusions = listOf("#title == 'test1234'")
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("11111")))
    }

    @Test
    fun test_multiple() {
        val sourceItem = sourceItem("222Test111")
        val filter = ExpressionItemFilterSupplier.expressions(
            exclusions = listOf(
                "#date > &'2030-03-31'",
                "#title matches '.*qaz.*'"
            ),
            inclusions = listOf(
                "#date > &'2023-03-30'",
                "#title matches '.*Test.*'"
            ),
        )
        assertEquals(true, filter.test(sourceItem))
        assertEquals(false, filter.test(sourceItem.copy("1wsaqazfff")))
    }
}