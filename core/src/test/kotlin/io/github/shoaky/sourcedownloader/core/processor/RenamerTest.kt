@file:OptIn(ExperimentalPathApi::class)

package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.component.ForceTrimmer
import io.github.shoaky.sourcedownloader.component.provider.RegexVariable
import io.github.shoaky.sourcedownloader.component.provider.RegexVariableProvider
import io.github.shoaky.sourcedownloader.component.replacer.RegexVariableReplacer
import io.github.shoaky.sourcedownloader.component.replacer.WindowsPathReplacer
import io.github.shoaky.sourcedownloader.core.expression.CelCompiledExpressionFactory
import io.github.shoaky.sourcedownloader.core.expression.sourceFileDefs
import io.github.shoaky.sourcedownloader.core.file.*
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalPathApi::class)
class RenamerTest {

    private val defaultRenamer = Renamer()

    @Test
    fun given_empty_should_filename_use_origin_name() {
        val raw = createRawFileContent()
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals("1.txt", content.targetFilename)
        assertEquals(sourceSavePath.resolve("1.txt"), content.targetPath())
    }

    @Test
    fun given_constant_pattern_should_filename_expected() {
        val raw = createRawFileContent(
            filenamePattern = CorePathPattern("2")
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val resolve = sourceSavePath.resolve(Path("2.txt"))
        val targetPath = content.targetPath()
        assertEquals(resolve, targetPath)
    }

    @Test
    fun given_vars_pattern_should_filename_expected() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("date" to "2022-01-01", "name" to "2")),
            filenamePattern = CorePathPattern("{date} - {name}")
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val targetFilename = content.targetFilename()
        assertEquals("2022-01-01 - 2.txt", targetFilename)
    }

    @Test
    fun given_vars_pattern_should_target_expected() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(
                mapOf(
                    "date" to "2022-01-01",
                    "work" to "test",
                    "year" to "2022",
                    "title" to "123"
                )
            ),
            savePathPattern = CorePathPattern("{year}/{work}"),
            filenamePattern = CorePathPattern("{date} - {title}")
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val targetFilePath = content.targetPath()
        val expected = sourceSavePath.resolve(Path("2022", "test", "2022-01-01 - 123.txt"))
        assertEquals(expected, targetFilePath)
    }

    @Test
    fun given_2depth_should_equals() {
        val raw = createRawFileContent(
            savePathPattern = CorePathPattern("{name}/S{season}"),
            patternVariables = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(sourceSavePath.resolve("test"), content.fileSaveRootDirectory())
    }

    @Test
    fun given_1depth_or_empty_should_null() {
        val raw = createRawFileContent(
            savePathPattern = CorePathPattern("{name}"),
            patternVariables = MapPatternVariables(mapOf("name" to "test", "season" to "01"))
        )
        val content1 = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(sourceSavePath.resolve("test"), content1.fileSaveRootDirectory())
        val content2 = defaultRenamer.createFileContent(
            sourceItem(),
            raw.copy(savePathPattern = CorePathPattern.origin),
            RenameVariables.EMPTY
        )
        assertEquals(null, content2.fileSaveRootDirectory())
    }

    @Test
    fun given_shared_vars() {
        val raw = createRawFileContent(
            savePathPattern = CorePathPattern("{name}/S{season}"),
            patternVariables = MapPatternVariables(mapOf("name" to "test"))
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables(mapOf("season" to "01")))
        val saveDir = content.saveDirectoryPath()
        assertEquals(sourceSavePath.resolve(Path("test", "S01")), saveDir)
    }

    @Test
    fun test_downloadItemFileRootDirectory() {
        val raw = createRawFileContent()
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertNull(content.fileDownloadRootDirectory())
        val content1 = content.copy(
            fileDownloadPath = Path("src", "test", "resources", "downloads", "easd", "222", "1.txt"),
            downloadPath = downloadPath
        )
        assertEquals(downloadPath.resolve("easd"), content1.fileDownloadRootDirectory())
    }

    @Test
    fun given_extension_pattern_should_expected() {
        val raw = createRawFileContent(
            filePath = Path("src", "test", "resources", "downloads", "easd", "222", "1.mp4"),
            filenamePattern = CorePathPattern("{name} - {season}.mp4"),
            patternVariables = MapPatternVariables(mapOf("name" to "test"))
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables(mapOf("season" to "01")))
        assertEquals("test - 01.mp4", content.targetFilename())
    }

    @Test
    fun test_item_download_root_directory() {
        val c2 = defaultRenamer.createFileContent(sourceItem(), createRawFileContent(), RenameVariables.EMPTY)
        val d2 = c2.fileDownloadRootDirectory()
        assertEquals(null, d2)
    }

    @Test
    fun test_variable_error_given_stay_strategy() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePattern = CorePathPattern("{name} - {season}"),
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(content.fileDownloadPath, content.targetPath())
    }

    @Test
    fun test_variable_error_given_stay_strategy2() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("S{season}"),
            filenamePattern = CorePathPattern("{name} - {season}"),
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(content.fileDownloadPath, content.targetPath())
    }

    @Test
    fun test_variable_error_given_pattern_strategy() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePattern = CorePathPattern("{name} - {season}"),
        )
        val content = Renamer(VariableErrorStrategy.PATTERN).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        println(content.targetPath())
        val resolve = sourceSavePath.resolve("{name}").resolve("S01").resolve("{name} - 01.txt")
        assertEquals(resolve, content.targetPath())
    }

    @Test
    fun test_both_variable_error_given_original_strategy() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePattern = CorePathPattern("{name} - {season}"),
        )
        val content =
            Renamer(VariableErrorStrategy.ORIGINAL).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        println(content.targetPath())
        assertEquals(content.fileDownloadPath, content.targetPath())
    }

    @Test
    fun test_filename_variable_error_given_original_strategy() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01", "name" to "test")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePattern = CorePathPattern("{title} - {season}"),
        )
        val content =
            Renamer(VariableErrorStrategy.ORIGINAL).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val resolve = sourceSavePath.resolve("test").resolve("S01").resolve("1.txt")
        assertEquals(resolve, content.targetPath())
    }

    @Test
    fun test_save_path_variable_error_given_original_strategy() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01", "title" to "test 01")),
            savePathPattern = CorePathPattern("{name}/S{season}"),
            filenamePattern = CorePathPattern("{title} - {season}"),
        )
        val content =
            Renamer(VariableErrorStrategy.ORIGINAL).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(downloadPath.resolve("test 01 - 01.txt"), content.targetPath())
    }

    @Test
    fun test_item_download_relative_parent_directory() {
        val raw = createRawFileContent(
            Path("FATE", "Season 01", "EP01.mp4"),
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        assertEquals(Path("FATE", "Season 01"), content.fileDownloadRelativeParentDirectory())
    }

    @Test
    fun given_unresolved_filename_with_dir_item() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01", "title" to "test 01")),
            savePathPattern = CorePathPattern("{title}/S{season}"),
            filenamePattern = CorePathPattern("{title} S{season}E{episode}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("test 01", "S01", "unresolved", "1.txt")
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun given_unresolved_filename_with_not_dir_item() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(mapOf("season" to "01", "title" to "test 01")),
            savePathPattern = CorePathPattern(""),
            filenamePattern = CorePathPattern("{title} S{season}E{episode}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("unresolved", "1.txt")
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun given_unresolved_save_path_with_dir_item() {
        val raw = createRawFileContent(
            Path("FATE", "AAAAA.mp4"),
            patternVariables = MapPatternVariables(mapOf("season" to "01", "episode" to "02")),
            savePathPattern = CorePathPattern("{title}"),
            filenamePattern = CorePathPattern("S{season}E{episode}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("unresolved", "FATE", "S01E02.mp4")
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun given_unresolved_save_path_with_no_dir_item() {
        val raw = createRawFileContent(
            Path("FATE", "AAAAA.mp4"),
            patternVariables = MapPatternVariables(mapOf("season" to "01", "episode" to "02")),
            savePathPattern = CorePathPattern("{title}"),
            filenamePattern = CorePathPattern("S{season}E{episode}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("unresolved", "FATE", "S01E02.mp4")
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun given_both_unresolved_with_dir_item() {
        val raw = createRawFileContent(
            Path("FATE", "AAAAA.mp4"),
            patternVariables = MapPatternVariables(mapOf("season" to "01", "episode" to "02")),
            savePathPattern = CorePathPattern("{Title}"),
            filenamePattern = CorePathPattern("S{Season}E{Episod}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("unresolved", "FATE", "AAAAA.mp4")
        println(content.targetPath())
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun given_both_unresolved_with_no_dir_item() {
        val raw = createRawFileContent(
            Path("AAAAA.mp4"),
            patternVariables = MapPatternVariables(mapOf("season" to "01", "episode" to "02")),
            savePathPattern = CorePathPattern("{Title}"),
            filenamePattern = CorePathPattern("S{Season}E{Episod}"),
        )
        val content =
            Renamer(VariableErrorStrategy.TO_UNRESOLVED).createFileContent(sourceItem(), raw, RenameVariables.EMPTY)
        val path = Path("unresolved", "AAAAA.mp4")
        assertEquals(content.sourceSavePath.resolve(path), content.targetPath())
    }

    @Test
    fun normal_parse() {
        val pathPattern = CorePathPattern("{name}/{title}abc")
        val variables = mapOf(
            "name" to "111",
            "title" to "test"
        )

        val parseResult = defaultRenamer.parse(variables, pathPattern)
        assertEquals("111/testabc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = mapOf(
            "name" to "111",
            "title" to "test"
        )
        val parseResult = defaultRenamer.parse(variables, pathPattern)
        assertEquals("111/testabc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_option_pattern_with_not_exists_variables() {
        val pathPattern = CorePathPattern("{name}/:{title}abc")
        val variables = mapOf(
            "name" to "111",
        )
        val parseResult = defaultRenamer.parse(variables, pathPattern)
        assertEquals("111/abc", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
    }

    @Test
    fun given_expression() {
        val pathPattern = CorePathPattern("{'test '+name} E{episode + '1'}:{' - '+source}")
        val variables = mapOf(
            "name" to "111",
            "episode" to "2",
            "source" to "1"
        )
        val parseResult = defaultRenamer.parse(variables, pathPattern)
        assertEquals("test 111 E21 - 1", parseResult.path)
        assertEquals(true, parseResult.results.all { it.success })
        val variables2 = mapOf(
            "name" to "111",
            "episode" to "2",
        )
        val result2 = defaultRenamer.parse(variables2, pathPattern)
        assertEquals("test 111 E21", result2.path)
        assertEquals(true, result2.results.all { it.success })
    }

    @Test
    fun test_replacement_given_pattern_variables() {
        val renamer = Renamer(
            variableReplacers = listOf(
                RegexVariableReplacer("BDRIP".toRegex(RegexOption.IGNORE_CASE), "BD"),
            )
        )
        val result = renamer.parse(
            mapOf(
                "title" to "111",
                "source" to "Web",
            ),
            CorePathPattern("{title}-{source}")
        )
        assertEquals("111-Web", result.path)
    }

    @Test
    fun test_replacement_given_pattern_variables_and_extra_variables() {
        val renamer = Renamer(
            variableReplacers = listOf(
                RegexVariableReplacer("BDRIP".toRegex(RegexOption.IGNORE_CASE), "BD"),
                RegexVariableReplacer("333".toRegex(RegexOption.IGNORE_CASE), "111")
            )
        )

        val itemVars = renamer.itemRenameVariables(
            sourceItem(attrs = mapOf("title" to "333")),
            MapPatternVariables(mapOf("source" to "BDrip"))
        )
        val result = renamer.createFileContent(
            sourceItem(),
            createRawFileContent(filenamePattern = CorePathPattern("{item.attrs.title}-{source}")),
            itemVars,

            )

        assertEquals("111-BD", result.targetPath().nameWithoutExtension)
    }

    @Test
    fun given_attr_variables() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(
                mapOf("date" to "2022-01-01", "work" to "test", "year" to "2022", "title" to "123")
            ),
            savePathPattern = CorePathPattern("{item.attrs.creatorId}/{date}"),
            filenamePattern = CorePathPattern("{file.attrs.seq}"),
            attrs = mapOf("seq" to "2")
        )
        val itemVars = defaultRenamer.itemRenameVariables(
            sourceItem(
                attrs = mapOf("creatorId" to "Idk111")
            ),
            PatternVariables.EMPTY
        )
        val content = defaultRenamer.createFileContent(sourceItem(), raw, itemVars)
        val targetFilePath = content.targetPath()
        val expected = sourceSavePath.resolve(Path("Idk111", "2022-01-01", "2.txt"))
        assertEquals(expected, targetFilePath)
    }

    @Test
    fun given_origin_layout_pattern() {
        val file1 = createRawFileContent(
            filePath = Path("wp", "mp3", "origin", "1.mp3"),
            savePathPattern = CorePathPattern("wp-test/{file.originalLayout}"),
        )

        val renamer = Renamer(
            variableReplacers = listOf(
                WindowsPathReplacer
            )
        )
        val content1 = renamer.createFileContent(sourceItem(), file1, RenameVariables.EMPTY)
        assertEquals(sourceSavePath.resolve(Path("wp-test", "mp3", "origin", "1.mp3")), content1.targetPath())

        val file2 = createRawFileContent(
            filePath = Path("wp", "1.mp3"),
            savePathPattern = CorePathPattern("wp-test/{file.originalLayout}"),
        )
        val content2 = renamer.createFileContent(sourceItem(), file2, RenameVariables.EMPTY)
        assertEquals(sourceSavePath.resolve(Path("wp-test", "1.mp3")), content2.targetPath())

        val file3 = createRawFileContent(
            filePath = Path("1.mp3"),
            savePathPattern = CorePathPattern("wp-test/{file.originalLayout}"),
        )
        val content3 = renamer.createFileContent(sourceItem(), file3, RenameVariables.EMPTY)
        assertEquals(sourceSavePath.resolve(Path("wp-test", "1.mp3")), content3.targetPath())
    }

    @Test
    fun test_variable_process() {
        val renamer = Renamer(
            variableProcessChain = listOf(
                VariableProcessChain(
                    input = "custom",
                    chain = listOf(
                        RegexVariableProvider(
                            listOf(
                                RegexVariable("custom1", "\\[.*]".toRegex()),
                                RegexVariable("number", "\\d+".toRegex()),
                                RegexVariable("filter1", "\\d+".toRegex()),
                            ),
                            "custom1"
                        ),
                        RegexVariableProvider(
                            listOf(
                                RegexVariable("custom2", "\\d+".toRegex()),
                            ),
                            "custom2"
                        )
                    ),
                    output = VariableProcessOutput(
                        keyMapping = mapOf("custom" to "custom_name", "number" to "numbers"),
                        excludeKeys = setOf("filter1"),
                    ),
                    condition = CelCompiledExpressionFactory.create(
                        "file.name == 1",
                        Boolean::class.java,
                        sourceFileDefs()
                    )
                )
            )
        )

        val itemVars = renamer.itemRenameVariables(
            sourceItem("aaaa ddd [1234]"),
            MapPatternVariables().also { it.addVariable("custom", "aaaa ddd [1234]") }
        )
        val fileContent = renamer.createFileContent(
            sourceItem(),
            createRawFileContent(filenamePattern = CorePathPattern("{custom_name}")),
            itemVars
        )
        // For Chain
        assertEquals("1234.txt", fileContent.targetFilename)
        val processed = fileContent.processedVariables?.variables() ?: emptyMap()
        assertEquals("1234", processed["custom_name"])
        // For key mapping
        assertEquals("1234", processed["numbers"])
        // For exclude
        assert(processed.containsKey("filter1").not())

        // For condition
        // 该功能还有歧义暂时关闭
        // val itemVars2 = renamer.itemRenameVariables(
        //     sourceItem("aaaa ddd [1234]"),
        //     MapPatternVariables().also { it.addVariable("custom", "aaaa ddd [1234]") }
        // )
        // val content2 = renamer.createFileContent(
        //     createRawFileContent(filePath = Path("2.txt"), filenamePattern = CorePathPattern("{custom_name}")),
        //     itemVars2
        // )
        // assertEquals("2.txt", content2.targetFilename)
    }

    @Test
    fun given_dot_filename_should_no_extension() {
        val file = createRawFileContent(
            filePath = Path(
                "downloads",
                "xxx",
                "_____padding_file_0_如果您看到此文件，请升级到BitComet(比特彗星)0.85或以上版本____"
            ),
            filenamePattern = CorePathPattern("test"),
        )
        val fileContent = defaultRenamer.createFileContent(sourceItem(), file, RenameVariables.EMPTY)
        assertEquals("test", fileContent.targetFilename)
    }

    @Test
    fun given_too_long_variables_should_trim() {
        val raw = createRawFileContent(
            patternVariables = MapPatternVariables(
                // 生成大于255的长度
                mapOf("title" to (1..150).joinToString(separator = "") { it.toString() })
            ),
            savePathPattern = CorePathPattern("test/111111112222222222{title}"),
            filenamePattern = CorePathPattern("{title}"),
        )

        val renamer = Renamer(trimming = mapOf("title" to listOf(ForceTrimmer)))
        val itemVars = renamer.itemRenameVariables(
            sourceItem(),
            PatternVariables.EMPTY
        )
        val content = renamer.createFileContent(sourceItem(), raw, itemVars)
        val expectSize = 255
        assert(content.targetFilename.toByteArray().size <= expectSize)
        assert(content.patternVariables.getVariables()["trimmed_title"] != null)
        val variablePathSize = sourceSavePath.relativize(content.saveDirectoryPath()).getName(1).name.toByteArray().size
        assert(variablePathSize <= expectSize)
        assertEquals(expectSize, variablePathSize)
    }

    companion object {

        val sourceSavePath: Path = testResourcePath.resolve("target")
        val downloadPath: Path = testResourcePath.resolve("downloads")

        @JvmStatic
        @AfterAll
        fun clean() {
            sourceSavePath.deleteRecursively()
            downloadPath.deleteRecursively()
        }
    }

}

fun createRawFileContent(
    filePath: Path = Path("1.txt"),
    sourceSavePath: Path = testResourcePath.resolve("target"),
    downloadPath: Path = testResourcePath.resolve("downloads"),
    patternVariables: MapPatternVariables = MapPatternVariables(),
    savePathPattern: CorePathPattern = CorePathPattern.origin,
    filenamePattern: CorePathPattern = CorePathPattern.origin,
    attrs: Map<String, Any> = emptyMap(),
    tags: Set<String> = emptySet()
): RawFileContent {
    return RawFileContent(
        sourceSavePath,
        downloadPath,
        patternVariables,
        savePathPattern,
        filenamePattern,
        SourceFile(filePath, attrs, tags = tags)
    )
}