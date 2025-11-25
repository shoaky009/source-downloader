package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class VariableProvidersAggregationTest {

    @Test
    fun test_vote() {
        val p1 = CustomProvider(
            mapOf("seasonNumber" to "01", "test1" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02")),
                MapPatternVariables(mapOf("episode" to "03")),
            )
        )

        val p2 = CustomProvider(
            mapOf("seasonNumber" to "01", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "03")),
            )
        )

        val p3 = CustomProvider(
            mapOf("season" to "02", "test3" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "03", "test" to "111")),
            )
        )

        val aggregation = VariableProvidersAggregation(
            sourceItem(),
            listOf(p1, p2, p3),
            VariableConflictStrategy.VOTE,
            mapOf("seasonNumber" to "season")
        )

        val shared = aggregation.itemVariables(sourceItem())
        val sharedVars = shared.variables()
        assertEquals(4, sharedVars.size)
        assertEquals("01", sharedVars["season"])

        val sourceFiles = aggregation.fileVariables(
            sourceItem(),
            shared,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )

        assertEquals("01", sourceFiles[0].variables()["episode"])
        assertEquals("02.5", sourceFiles[1].variables()["episode"])
        assertEquals("03", sourceFiles[2].variables()["episode"])
        assertEquals(2, sourceFiles[2].variables().size)
        assertEquals("111", sourceFiles[2].variables()["test"])
    }

    @Test
    fun test_accuracy() {
        val p1 = CustomProvider(
            mapOf("season" to "01", "test1" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            0
        )

        val p2 = CustomProvider(
            mapOf("season" to "01", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episodeNumber" to "01", "test1" to "test1")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            1
        )

        val p3 = CustomProvider(
            mapOf("seasonNumber" to "02", "test3" to "2"),
            listOf(
                MapPatternVariables(mapOf("episodeNumber" to "01")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "03", "test" to "111")),
            ),
            2
        )

        val p4 = CustomProvider(
            mapOf("season" to "01", "test4" to "4"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "dddd" to "4444")),
                MapPatternVariables(mapOf("episodeNumber" to "02")),
                MapPatternVariables(mapOf("episodeNumber" to "04")),
            ),
            1
        )

        val aggregation = VariableProvidersAggregation(
            sourceItem(),
            listOf(p1, p2, p3, p4),
            VariableConflictStrategy.ACCURACY,
            mapOf("seasonNumber" to "season", "episodeNumber" to "episode")
        )
        val shared = aggregation.itemVariables(sourceItem())
        val sharedVariables = shared.variables()

        assertEquals(5, sharedVariables.size)
        assertEquals("02", sharedVariables["season"])
        assertEquals("2", sharedVariables["test3"])

        val sourceFiles = aggregation.fileVariables(
            sourceItem(),
            shared,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )

        assertEquals("01", sourceFiles[0].variables()["episode"])
        assertEquals("03", sourceFiles[2].variables()["episode"])
        assertEquals(3, sourceFiles[0].variables().size)
    }

    @Test
    fun test_smart_given_2providers() {
        val p1 = CustomProvider(
            mapOf("season" to "01", "test1" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            0
        )

        val p2 = CustomProvider(
            mapOf("season" to "02", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "key2" to "test2")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            1
        )

        val aggregation1 = VariableProvidersAggregation(sourceItem(), listOf(p1, p2))
        val shared1 = aggregation1.itemVariables(sourceItem())
        val sharedVariables1 = shared1.variables()
        assertEquals(3, sharedVariables1.size)
        assertEquals("02", sharedVariables1["season"])

        val sourceFiles1 = aggregation1.fileVariables(
            sourceItem(),
            shared1,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )

        assertEquals(2, sourceFiles1[0].variables().size)
        assertEquals("test2", sourceFiles1[0].variables()["key2"])
        assertEquals("02.5", sourceFiles1[1].variables()["episode"])

        // change order
        val aggregation2 = VariableProvidersAggregation(sourceItem(), listOf(p2, p1))
        val shared2 = aggregation2.itemVariables(sourceItem())
        val sharedVariables2 = shared2.variables()
        assertEquals(3, sharedVariables2.size)
        assertEquals("02", sharedVariables2["season"])

        val sourceFiles2 = aggregation2.fileVariables(
            sourceItem(),
            shared2,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )

        assertEquals(2, sourceFiles2[0].variables().size)
        assertEquals("test2", sourceFiles2[0].variables()["key2"])
        assertEquals("02.5", sourceFiles2[1].variables()["episode"])

    }

    @Test
    fun test_smart_given_4providers() {
        val p1 = CustomProvider(
            mapOf("season" to "00", "test1" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            0
        )

        val p2 = CustomProvider(
            mapOf("season" to "01", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "key2" to "test2")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            2
        )

        val p3 = CustomProvider(
            mapOf("season" to "02", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "key2" to "test2")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04", "key4.1" to "5")),
            ),
            2
        )

        val p4 = CustomProvider(
            mapOf("season" to "02", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "key2" to "test2")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04", "key4" to "2", "key4.1" to "4")),
            ),
            1
        )

        val p5 = CustomProvider(
            mapOf("season" to "02", "test2" to "3"),
            listOf(
                MapPatternVariables(),
                MapPatternVariables(),
                MapPatternVariables()
            ),
            4
        )

        val aggregation1 = VariableProvidersAggregation(sourceItem(), listOf(p1, p3, p2, p4, p5))
        val shared1 = aggregation1.itemVariables(sourceItem())
        val sharedVariables1 = shared1.variables()
        assertEquals(3, sharedVariables1.size)
        assertEquals("02", sharedVariables1["season"])
        assertEquals("3", sharedVariables1["test2"])

        val sourceFiles1 = aggregation1.fileVariables(
            sourceItem(),
            shared1,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )
        assertEquals(2, sourceFiles1[0].variables().size)
        assertEquals(3, sourceFiles1[2].variables().size)
        assertEquals("5", sourceFiles1[2].variables()["key4.1"])

        // change order
        val aggregation2 = VariableProvidersAggregation(sourceItem(), listOf(p5, p4, p3, p1, p2))
        val shared2 = aggregation2.itemVariables(sourceItem())
        val sharedVariables2 = shared2.variables()
        assertEquals(3, sharedVariables2.size)
        assertEquals("02", sharedVariables2["season"])
        assertEquals("3", sharedVariables1["test2"])

        val sourceFiles2 = aggregation2.fileVariables(
            sourceItem(),
            shared2,
            listOf(
                SourceFile(Path("")),
                SourceFile(Path("")),
                SourceFile(Path("")),
            )
        )
        assertEquals(2, sourceFiles2[0].variables().size)
        assertEquals(3, sourceFiles2[2].variables().size)
        assertEquals("5", sourceFiles2[2].variables()["key4.1"])

    }

    @Test
    fun same_accuracy_in_order() {
        val p1 = CustomProvider(
            mapOf("season" to "00", "test1" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01")),
                MapPatternVariables(mapOf("episode" to "02")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            2
        )
        val p2 = CustomProvider(
            mapOf("season" to "01", "test2" to "2"),
            listOf(
                MapPatternVariables(mapOf("episode" to "01", "key2" to "test2")),
                MapPatternVariables(mapOf("episode" to "02.5")),
                MapPatternVariables(mapOf("episode" to "04")),
            ),
            2
        )

        val aggregation = VariableProvidersAggregation(sourceItem(), listOf(p1, p2))
        val sharedVariables = aggregation.itemVariables(sourceItem()).variables()
        assertEquals("00", sharedVariables["season"])
    }
}

private class CustomProvider(
    val variables: Map<String, String>,
    val fileVariables: List<PatternVariables>,
    override val accuracy: Int = 2
) : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables {
        return MapPatternVariables(variables)
    }

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>
    ): List<PatternVariables> {
        return fileVariables
    }

    override fun primaryVariableName(): String? {
        return null
    }

}