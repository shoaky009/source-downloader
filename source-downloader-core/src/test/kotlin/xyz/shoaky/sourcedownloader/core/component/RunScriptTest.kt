package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.*
import java.net.URL
import kotlin.io.path.Path
import kotlin.test.assertEquals

class RunScriptTest {
    @Test
    fun run_script() {
        val os = System.getProperty("os.name")
        val scriptPath = if (os.contains("windows", true))
            Path("src/test/resources/script/test.ps1") else
            Path("src/test/resources/script/test.sh")
        val runScript = RunScript(scriptPath)

        val item = SourceItem("1", URL("http://localhost"), "", URL("http://localhost"))
        val content = SourceFileContent(
            Path(""),
            Path(""),
            PatternVars(mapOf("date" to "2022-01-01", "name" to "test")),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )

        val process = runScript.run(SourceContent(item, listOf(content)))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("2022-01-01 test", result)
    }
}