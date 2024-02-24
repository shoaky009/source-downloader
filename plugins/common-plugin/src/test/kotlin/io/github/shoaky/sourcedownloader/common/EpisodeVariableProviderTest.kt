package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.supplier.EpisodeVariableProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path

class EpisodeVariableProviderTest {

    @Test
    fun should_all_expected() {
        val provider = EpisodeVariableProviderSupplier.apply(
            CoreContext.empty,
            Properties.empty
        )
        val item = sourceItem()
        Files.readAllLines(Path("src", "test", "resources", "episode-test-data.csv"))
            .filter { it.isNullOrBlank().not() }
            .map {
                val split = it.split(",")
                Pair(split[0], split[1])
            }
            .forEach {
                val name = it.second

                val group = provider.itemSharedVariables(item)
                val file = provider.itemFileVariables(item, group, listOf(SourceFile(Path(name)))).first
                assertEquals(it.first, file.variables()["episode"])
            }
    }
}