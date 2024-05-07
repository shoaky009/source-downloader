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
            .forEach { (episode, name) ->
                val group = provider.itemVariables(item)
                val sf = SourceFile(Path(name))
                val file = provider.fileVariables(item, group, listOf(sf)).first()
                assertEquals(episode, file.variables().getOrDefault("episode", ""), "name: $name")
            }
    }
}