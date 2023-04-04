package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.core.idk.PersistentFileContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
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
            patternVars = MapPatternVariables(mapOf("date" to "2022-01-01", "name" to "2")),
            filenamePathPattern = PathPattern("{date} - {name}")
        )
        val targetFilename = content.targetFilename()
        assertEquals("2022-01-01 - 2.txt", targetFilename)
    }

    @Test
    fun given_empty_should_target_expected() {
        val content = createFileContent()
        val targetFilePath = content.targetPath()
        assertEquals(sourceSavePath.resolve("1.txt"), targetFilePath)
    }

    @Test
    fun given_constant_should_target_expected() {
        val fileContent = createFileContent(
            savePathPattern = PathPattern("2"),
            filenamePathPattern = PathPattern("3")
        )
        val targetFilePath = fileContent.targetPath()
        assertEquals(sourceSavePath.resolve("2/3.txt"), targetFilePath)
    }

    @Test
    fun given_vars_pattern_should_target_expected() {
        val fileContent = createFileContent(
            patternVars = MapPatternVariables(mapOf("date" to "2022-01-01", "work" to "test", "year" to "2022", "title" to "123")),
            savePathPattern = PathPattern("{year}/{work}"),
            filenamePathPattern = PathPattern("{date} - {title}")
        )
        val targetFilePath = fileContent.targetPath()
        val expected = sourceSavePath.resolve("2022/test/2022-01-01 - 123.txt")
        assertEquals(expected, targetFilePath)
    }

    @Test
    fun given_2depth_should_equals() {
        val createFileContent = createFileContent(
            savePathPattern = PathPattern("{name}/S{season}"),
            patternVars = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        assertEquals(sourceSavePath.resolve("test"), createFileContent.itemFileRootDirectory())
    }

    @Test
    fun given_1depth_or_empty_should_null() {
        val content1 = createFileContent(
            savePathPattern = PathPattern("{name}"),
            patternVars = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        assertEquals(null, content1.itemFileRootDirectory())

        val content2 = content1.copy(fileSavePathPattern = PathPattern.ORIGIN)
        assertEquals(null, content2.itemFileRootDirectory())
    }

}

private fun createFileContent(
    fileDownloadPath: Path = Path("src/test/resources/downloads/1.txt"),
    sourceSavePath: Path = Path("src/test/resources/target"),
    patternVars: MapPatternVariables = MapPatternVariables(),
    savePathPattern: PathPattern = PathPattern.ORIGIN,
    filenamePathPattern: PathPattern = PathPattern.ORIGIN): PersistentFileContent {
    return PersistentFileContent(
        fileDownloadPath,
        sourceSavePath,
        patternVars,
        savePathPattern,
        filenamePathPattern,
    )
}