package xyz.shoaky.sourcedownloader.sdk

import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals

class SourceFileContentTest {

    private val testFilePath = Path("src/test/resources/downloads/1.txt")
    private val sourceSavePath = Path("src/test/resources/target")

    @Test
    fun given_empty_should_filename_use_origin_name() {
        val content = createFileContent()
        val targetFilename = content.targetFilename()
        assertEquals("1.txt", targetFilename)
    }

    @Test
    fun given_constant_pattern_should_filename_expected() {
        val content = createFileContent(
            filenamePathPattern = PathPattern("2")
        )
        val targetFilename = content.targetFilename()
        assertEquals("2.txt", targetFilename)
    }

    @Test
    fun given_vars_pattern_should_filename_expected() {
        val content = createFileContent(
            patternVars = PatternVars(mapOf("date" to "2022-01-01", "name" to "2")),
            filenamePathPattern = PathPattern("{date} - {name}")
        )
        val targetFilename = content.targetFilename()
        assertEquals("2022-01-01 - 2.txt", targetFilename)
    }

    @Test
    fun given_empty_should_target_expected() {
        val content = createFileContent()
        val targetFilePath = content.targetFilePath()
        assertEquals(sourceSavePath.resolve("1.txt"), targetFilePath)
    }

    @Test
    fun given_constant_should_target_expected() {
        val fileContent = createFileContent(
            savePathPattern = PathPattern("2"),
            filenamePathPattern = PathPattern("3")
        )
        val targetFilePath = fileContent.targetFilePath()
        assertEquals(sourceSavePath.resolve("2/3.txt"), targetFilePath)
    }

    @Test
    fun given_vars_pattern_should_target_expected() {
        val fileContent = createFileContent(
            patternVars = PatternVars(mapOf("date" to "2022-01-01", "work" to "test", "year" to "2022", "title" to "123")),
            savePathPattern = PathPattern("{year}/{work}"),
            filenamePathPattern = PathPattern("{date} - {title}")
        )
        val targetFilePath = fileContent.targetFilePath()
        val expected = sourceSavePath.resolve("2022/test/2022-01-01 - 123.txt")
        assertEquals(expected, targetFilePath)
    }

}

private fun createFileContent(
    fileDownloadPath: Path = Path("src/test/resources/downloads/1.txt"),
    sourceSavePath: Path = Path("src/test/resources/target"),
    patternVars: PatternVars = PatternVars(),
    savePathPattern: PathPattern = PathPattern.ORIGIN,
    filenamePathPattern: PathPattern = PathPattern.ORIGIN): SourceFileContent {
    return SourceFileContent(
        fileDownloadPath,
        sourceSavePath,
        patternVars,
        savePathPattern,
        filenamePathPattern,
    )
}