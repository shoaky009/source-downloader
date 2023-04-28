package xyz.shoaky.sourcedownloader.component.provider

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.SeasonProviderSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
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
            .forEach {
                val name = it.second
                val group = sp.createSourceGroup(sourceItem(title = name))
                val sourceFiles = group.sourceFiles(
                    listOf(Path(it.third))
                )
                val season = sourceFiles.first().patternVariables().variables()["season"]
                assertEquals(it.first, season, "name:${name}")
            }
    }
}