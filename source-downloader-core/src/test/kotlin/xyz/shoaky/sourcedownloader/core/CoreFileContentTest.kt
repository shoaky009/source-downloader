package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.VariableErrorStrategy
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.testResourcePath
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalPathApi::class)
class CoreFileContentTest {

    @Test
    fun given_empty_should_filename_use_origin_name() {
        val content = createFileContent()
        val targetFilename = content.targetFilename()
        assertEquals("1.txt", targetFilename)
    }

    @Test
    fun given_constant_pattern_should_filename_expected() {
        val content = createFileContent(
            filenamePathPattern = CorePathPattern("2")
        )
        val targetFilename = content.targetFilename()
        assertEquals("2.txt", targetFilename)
    }

    @Test
    fun given_vars_pattern_should_filename_expected() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("date" to "2022-01-01", "name" to "2")),
            filenamePathPattern = CorePathPattern("{date} - {name}")
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
            savePathPattern = CorePathPattern("2"),
            filenamePathPattern = CorePathPattern("3")
        )
        val targetFilePath = fileContent.targetPath()
        assertEquals(sourceSavePath.resolve(Path("2", "3.txt")), targetFilePath)
    }

    @Test
    fun given_vars_pattern_should_target_expected() {
        val fileContent = createFileContent(
            patternVars = MapPatternVariables(mapOf("date" to "2022-01-01", "work" to "test", "year" to "2022", "title" to "123")),
            savePathPattern = CorePathPattern("{year}/{work}"),
            filenamePathPattern = CorePathPattern("{date} - {title}")
        )
        val targetFilePath = fileContent.targetPath()
        val expected = sourceSavePath.resolve(Path("2022", "test", "2022-01-01 - 123.txt"))
        assertEquals(expected, targetFilePath)
    }

    @Test
    fun given_2depth_should_equals() {
        val createFileContent = createFileContent(
            savePathPattern = CorePathPattern("{name}/S{season}"),
            patternVars = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        assertEquals(sourceSavePath.resolve("test"), createFileContent.itemSaveRootDirectory())
    }

    @Test
    fun given_1depth_or_empty_should_null() {
        val content1 = createFileContent(
            savePathPattern = CorePathPattern("{name}"),
            patternVars = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        assertEquals(null, content1.itemSaveRootDirectory())
        val content2 = content1.copy(fileSavePathPattern = CorePathPattern.ORIGIN)
        assertEquals(null, content2.itemSaveRootDirectory())
    }

    @Test
    fun given_shared_vars() {
        val fileContent = createFileContent(
            savePathPattern = CorePathPattern("{name}/S{season}"),
            patternVars = MapPatternVariables(mapOf("name" to "test")))
        fileContent.addSharedVariables(MapPatternVariables(mapOf("season" to "01")))

        val saveDir = fileContent.saveDirectoryPath()
        assertEquals(sourceSavePath.resolve(Path("test", "S01")), saveDir)
    }

    @Test
    fun test_downloadItemFileRootDirectory() {
        val content = createFileContent(
        )
        assertNull(content.itemDownloadRootDirectory())


        val content1 = content.copy(
            fileDownloadPath = Path("src", "test", "resources", "downloads", "easd", "222", "1.txt"),
            downloadPath = downloadPath
        )
        assertEquals(downloadPath.resolve("easd"), content1.itemDownloadRootDirectory())
    }

    @Test
    fun given_extension_pattern_should_expected() {
        val fileContent = createFileContent(
            fileDownloadPath = Path("src", "test", "resources", "downloads", "easd", "222", "1.mp4"),
            filenamePathPattern = CorePathPattern("{name} - {season}.mp4"),
            patternVars = MapPatternVariables(mapOf("name" to "test")))
        fileContent.addSharedVariables(MapPatternVariables(mapOf("season" to "01")))
        assertEquals("test - 01.mp4", fileContent.targetFilename())
    }

    @Test
    fun test_item_download_root_directory() {
        val c1 = createFileContent(downloadPath = testResourcePath)
        val d1 = c1.itemDownloadRootDirectory()
        assertEquals(testResourcePath.resolve("downloads"), d1)

        val c2 = createFileContent()
        val d2 = c2.itemDownloadRootDirectory()
        assertEquals(null, d2)
    }

    @Test
    fun test_variable_error_given_stay_strategy() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePathPattern = CorePathPattern("{name} - {season}"),
        )
        content.setVariableErrorStrategy(VariableErrorStrategy.STAY)
        assertEquals(content.fileDownloadPath, content.targetPath())
    }

    @Test
    fun test_variable_error_given_pattern_strategy() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePathPattern = CorePathPattern("{name} - {season}"),
        )
        content.setVariableErrorStrategy(VariableErrorStrategy.PATTERN)
        println(content.targetPath())
        val resolve = sourceSavePath.resolve("{name}").resolve("S01").resolve("{name} - 01.txt")
        assertEquals(resolve, content.targetPath())
    }

    @Test
    fun test_both_variable_error_given_original_strategy() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePathPattern = CorePathPattern("{name} - {season}"),
        )
        content.setVariableErrorStrategy(VariableErrorStrategy.ORIGINAL)
        println(content.targetPath())
        assertEquals(content.fileDownloadPath, content.targetPath())
    }

    @Test
    fun test_filename_variable_error_given_original_strategy() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("season" to "01", "name" to "test")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePathPattern = CorePathPattern("{title} - {season}"),
        )
        content.setVariableErrorStrategy(VariableErrorStrategy.ORIGINAL)
        val resolve = sourceSavePath.resolve("test").resolve("S01").resolve("1.txt")
        assertEquals(resolve, content.targetPath())
    }

    @Test
    fun test_save_path_variable_error_given_original_strategy() {
        val content = createFileContent(
            patternVars = MapPatternVariables(mapOf("season" to "01", "title" to "test 01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePathPattern = CorePathPattern("{title} - {season}"),
        )
        content.setVariableErrorStrategy(VariableErrorStrategy.ORIGINAL)
        assertEquals(downloadPath.resolve("test 01 - 01.txt"), content.targetPath())
    }

    @Test
    fun test_item_download_relative_parent_directory() {
        val content = createFileContent(
            testResourcePath.resolve("downloads").resolve("FATE")
                .resolve("Season 01").resolve("EP01.mp4")
        )
        assertEquals(Path("FATE", "Season 01"), content.itemDownloadRelativeParentDirectory())
    }

    companion object {
        private val sourceSavePath = Path("src", "test", "resources", "target")
        private val downloadPath = Path("src", "test", "resources", "downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            sourceSavePath.deleteRecursively()
            downloadPath.deleteRecursively()
        }
    }
}

private fun createFileContent(
    fileDownloadPath: Path = Path("src", "test", "resources", "downloads", "1.txt"),
    sourceSavePath: Path = testResourcePath.resolve("target"),
    downloadPath: Path = testResourcePath.resolve("downloads"),
    patternVars: MapPatternVariables = MapPatternVariables(),
    savePathPattern: PathPattern = CorePathPattern.ORIGIN,
    filenamePathPattern: PathPattern = CorePathPattern.ORIGIN): CoreFileContent {
    return CoreFileContent(
        fileDownloadPath,
        sourceSavePath,
        downloadPath,
        patternVars,
        savePathPattern,
        filenamePathPattern,
    )
}