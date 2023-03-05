package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem

class RegexSourceFilterTest {


    @Test
    fun normal() {
        val props = ComponentProps.fromJson("""{"regexes":["test", "a.c"]}""")

        val filter = RegexSourceItemFilterSupplier.apply(props)
        assert(!filter.test(sourceItem("dsadas test")))
        assert(!filter.test(sourceItem("abc")))
        assert(filter.test(sourceItem("12346,!.")))
    }
}