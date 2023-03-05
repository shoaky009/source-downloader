package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.PatternVars
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceFileContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path
import kotlin.test.assertEquals

class RunScriptTest {
    private val scriptPath = if (System.getProperty("os.name").contains("windows", true))
        Path("src/test/resources/script/test.ps1") else
        Path("src/test/resources/script/test.sh")

    private val runScript = RunScriptSupplier.apply(
        ComponentProps.fromMap(mapOf("path" to scriptPath))
    )

    @Test
    fun run_script() {
        val content = SourceFileContent(
            Path(""),
            Path(""),
            PatternVars(mapOf("date" to "2022-01-01", "name" to "test")),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )

        val process = runScript.run(SourceContent(sourceItem("1"), listOf(content)))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("2022-01-01 test", result)
    }
}