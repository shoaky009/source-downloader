package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.resolver.SystemFileResolver
import xyz.shoaky.sourcedownloader.component.supplier.SystemFileSourceSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import kotlin.io.path.Path

class SystemFileSourceTest {

    @Test
    fun given_mode0_should_has_sub_files() {
        val source = SystemFileSourceSupplier.apply(Properties.fromMap(
            mapOf(
                "path" to Path("src", "test", "kotlin", "xyz", "shoaky", "sourcedownloader"),
                "mode" to 0
            )
        ))

        assert(source.fetch().any {
            SystemFileResolver.resolveFiles(it).size > 1
        })
    }

    @Test
    fun given_mode1_should_not_has_sub_files() {
        val source = SystemFileSourceSupplier.apply(Properties.fromMap(
            mapOf(
                "path" to Path("src", "test", "kotlin", "xyz", "shoaky", "sourcedownloader"),
                "mode" to 1
            )
        ))

        assert(source.fetch().all {
            SystemFileResolver.resolveFiles(it).size == 1
        })
    }
}