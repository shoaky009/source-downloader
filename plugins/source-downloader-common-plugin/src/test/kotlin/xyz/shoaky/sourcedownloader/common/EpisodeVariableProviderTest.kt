package xyz.shoaky.sourcedownloader.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.common.supplier.EpisodeVariableProviderSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sourceItem
import java.nio.file.Files
import kotlin.io.path.Path

class EpisodeVariableProviderTest {

    @Test
    fun should_all_expected() {
        val provider = EpisodeVariableProviderSupplier.apply(
            Properties.EMPTY
        )

        Files.readAllLines(Path("src", "test", "resources", "episode-test-data.csv"))
            .filter { it.isNullOrBlank().not() }
            .map {
                val split = it.split(",")
                Pair(split[0], split[1])
            }
            .forEach {
                val name = it.second
                val group = provider.createSourceGroup(sourceItem())
                val file = group.filePatternVariables(SourceFile(Path(name))).first()
                assertEquals(it.first, file.patternVariables().variables()["episode"])
            }
    }
}