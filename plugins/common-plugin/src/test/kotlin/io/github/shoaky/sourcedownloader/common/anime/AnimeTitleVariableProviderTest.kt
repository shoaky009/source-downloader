package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sourceItem
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeTitleVariableProviderTest {

    @Test
    fun test() {
        val provider = AnimeTitleVariableProvider
        Files.readAllLines(Path("src", "test", "resources", "anime-title-test-data.csv"))
            .forEach {
                val split = it.split(",")
                val rawTitle = split[0]
                val expectedTitle = split[1]
                val expectedRomajiTitle = split.getOrNull(2)

                val variables = provider.itemVariables(sourceItem(rawTitle)).variables()
                assertEquals(expectedTitle, variables["title"])
                if (expectedRomajiTitle != null) {
                    assertEquals(expectedRomajiTitle, variables["romajiTitle"])
                }
            }
    }
}