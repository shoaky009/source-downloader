package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.RunCommandSupplier
import xyz.shoaky.sourcedownloader.core.CorePathPattern
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path
import kotlin.test.assertEquals

class RunCommandTest {
    private val command = if (System.getProperty("os.name").contains("windows", true))
        listOf("powershell.exe", Path("src", "test", "resources", "script", "test.ps1").toAbsolutePath().toString(), "test1")
    else
        listOf(Path("src", "test", "resources", "script", "test.sh").toAbsolutePath().toString(), "test1")

    private val runCommand = RunCommandSupplier.apply(
        Properties.fromMap(mapOf("command" to command))
    )

    private val content = CoreFileContent(
        Path("test.txt"),
        Path("test.txt"),
        Path(""),
        MapPatternVariables(mapOf("date" to "2022-01-01", "name" to "test")),
        CorePathPattern.ORIGIN,
        CorePathPattern.ORIGIN,
    )

    @Test
    fun run_command() {

        val process = runCommand.run(PersistentSourceContent(sourceItem("1"), listOf(content), MapPatternVariables()))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("test1", result)
    }

    @Test
    fun run_command_with_summary() {
        val apply = RunCommandSupplier.apply(
            Properties.fromMap(
                mapOf("command" to command,
                    "withSubjectSummary" to true
                ))
        )

        val process = apply.run(PersistentSourceContent(sourceItem("1"), listOf(content), MapPatternVariables()))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("test1 test.txt", result)
    }
}