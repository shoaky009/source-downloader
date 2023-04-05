package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.SystemFileSourceSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps

class SystemFileSourceTest {

    @Test
    fun given_mode0_should_has_subfiles() {
        val source = SystemFileSourceSupplier.apply(ComponentProps.fromMap(
            mapOf(
                "path" to "src/test/kotlin/xyz/shoaky/sourcedownloader",
                "mode" to 0
            )
        ))

        assert(source.fetch().any {
            val size = source.resolveFiles(it).size
            size > 1
        })
    }

    @Test
    fun given_mode1_should_not_has_subfiles() {
        val source = SystemFileSourceSupplier.apply(ComponentProps.fromMap(
            mapOf(
                "path" to "src/test/kotlin/xyz/shoaky/sourcedownloader",
                "mode" to 1
            )
        ))

        assert(source.fetch().all {
            source.resolveFiles(it).size == 1
        })
    }
}