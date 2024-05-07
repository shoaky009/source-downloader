package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals

class AnimeTaggerTest {

    @Test
    fun test() {
        Files.readAllLines(Path("src", "test", "resources", "anime-tagger-data.csv"))
            .forEach { line ->
                val split = line.split(",")
                val expected = split[0].takeIf { it.isNotEmpty() }
                val path = split[1]
                assertEquals(expected, AnimeTagger.tag(SourceFile(Path(path))))
            }
    }
}