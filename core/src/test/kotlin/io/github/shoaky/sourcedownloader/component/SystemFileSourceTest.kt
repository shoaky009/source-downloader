package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.resolver.SystemFileResolver
import io.github.shoaky.sourcedownloader.component.supplier.SystemFileSourceSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class SystemFileSourceTest {

    @Test
    fun given_mode0_should_has_sub_files() {
        val source = SystemFileSourceSupplier.apply(
            CoreContext.empty,
            Properties.fromMap(
                mapOf(
                    "path" to Path("src", "test", "kotlin", "io", "github", "shoaky", "sourcedownloader"),
                    "mode" to 0
                )
            )
        )

        assert(source.fetch().any {
            SystemFileResolver.resolveFiles(it).size > 1
        })
    }

    @Test
    fun given_mode1_should_not_has_sub_files() {
        val source = SystemFileSourceSupplier.apply(
            CoreContext.empty,
            Properties.fromMap(
                mapOf(
                    "path" to Path("src", "test", "kotlin", "io", "github", "shoaky", "sourcedownloader"),
                    "mode" to 1
                )
            )
        )

        assert(source.fetch().all {
            SystemFileResolver.resolveFiles(it).size == 1
        })
    }
}