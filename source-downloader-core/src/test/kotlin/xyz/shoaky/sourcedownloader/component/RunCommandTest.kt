package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.RunCommandSupplier
import xyz.shoaky.sourcedownloader.core.idk.PersistentFileContent
import xyz.shoaky.sourcedownloader.core.idk.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path
import kotlin.test.assertEquals

class RunCommandTest {
    private val command = if (System.getProperty("os.name").contains("windows", true))
        listOf("powershell.exe", Path("src/test/resources/script/test.ps1").toAbsolutePath().toString(), "test1")
    else
        listOf(Path("src/test/resources/script/test.sh").toAbsolutePath().toString(), "test1")

    private val runCommand = RunCommandSupplier.apply(
        ComponentProps.fromMap(mapOf("command" to command))
    )

    private val content = PersistentFileContent(
        Path("test.txt"),
        Path("test.txt"),
        MapPatternVariables(mapOf("date" to "2022-01-01", "name" to "test")),
        PathPattern.ORIGIN,
        PathPattern.ORIGIN,
    )

    @Test
    fun run_command() {

        val process = runCommand.run(PersistentSourceContent(sourceItem("1"), listOf(content)))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("test1", result)
    }

    @Test
    fun run_command_with_summary() {
        val apply = RunCommandSupplier.apply(
            ComponentProps.fromMap(
                mapOf("command" to command,
                    "withSubjectSummary" to true
                ))
        )

        val process = apply.run(PersistentSourceContent(sourceItem("1"), listOf(content)))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("test1 test.txt", result)
    }
}