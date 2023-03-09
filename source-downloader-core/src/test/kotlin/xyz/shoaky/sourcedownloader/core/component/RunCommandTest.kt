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

class RunCommandTest {
    private val command = if (System.getProperty("os.name").contains("windows", true))

        listOf("powershell.exe", Path("src/test/resources/script/test.ps1").toAbsolutePath().toString())
    else
        listOf(Path("src/test/resources/script/test.sh").toAbsolutePath().toString())

    private val runCommand = RunCommandSupplier.apply(
        ComponentProps.fromMap(mapOf("command" to command))
    )

    @Test
    fun run_command() {
        val content = SourceFileContent(
            Path(""),
            Path(""),
            PatternVars(mapOf("date" to "2022-01-01", "name" to "test")),
            PathPattern.ORIGIN,
            PathPattern.ORIGIN,
        )

        val process = runCommand.run(SourceContent(sourceItem("1"), listOf(content)))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("2022-01-01 test", result)
    }
}