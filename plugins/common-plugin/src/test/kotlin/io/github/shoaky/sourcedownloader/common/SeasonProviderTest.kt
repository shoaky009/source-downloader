package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.supplier.SeasonProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.assertEquals

class SeasonProviderTest {
    @Test
    fun should_all_expected() {
        val sp = SeasonProviderSupplier.apply(Properties.fromMap(emptyMap()))
        Files.readAllLines(Path("src", "test", "resources", "season-test-data.csv"))
            .filter { it.isNullOrBlank().not() }
            .map {
                val split = it.split(",")
                Triple(split[0], split[1], split.elementAtOrNull(2) ?: "")
            }
            .forEach { (expect, name, path) ->
                val group = sp.createSourceGroup(sourceItem(title = name))
                val sourceFiles = group.filePatternVariables(
                    SourceFile(Path(path))
                )
                val season = sourceFiles.first().patternVariables().variables()["season"]
                assertEquals(expect, season, "name:${name}")
            }
    }
}