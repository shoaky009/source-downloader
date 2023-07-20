package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.RunCommandSupplier
import io.github.shoaky.sourcedownloader.core.CorePathPattern
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

/**
 * This test may fail on Windows, because security policy may prevent running PowerShell scripts.
 */
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
        val process = runCommand.run(CoreItemContent(sourceItem("1"), listOf(content), MapPatternVariables()))
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

        val process = apply.run(CoreItemContent(sourceItem("1"), listOf(content), MapPatternVariables()))
        assertEquals(0, process.waitFor())
        val result = process.inputStream.bufferedReader().readText()
        assertEquals("test1 test.txt", result)
    }
}