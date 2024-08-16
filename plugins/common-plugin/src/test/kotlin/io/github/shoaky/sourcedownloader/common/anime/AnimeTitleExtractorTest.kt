package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sourceItem
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeTitleExtractorTest {

    @Test
    fun test() {
        val provider = AnimeTitleVariableProvider
        Files.readAllLines(Path("src", "test", "resources", "anime-title-test-data.csv"))
            .forEach {
                val (rawTitle, expectedTitle) = it.split(",")
                val variables = provider.itemVariables(sourceItem(rawTitle)).variables()
                assertEquals(expectedTitle, variables["title"])
            }
    }
}