package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.supplier.LanguageVariableProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.PatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class LanguageVariableProviderTest {

    private val languagePath = Path("src", "test", "resources", "language")

    @Test
    fun from_filename() {
        val provider = LanguageVariableProviderSupplier.apply(
            CoreContext.empty,
            Properties.empty
        )

        val item = sourceItem()
        val group = provider.itemVariables(item)

        val sourceFiles = provider.fileVariables(
            item, group, listOf(
                SourceFile(Path("dsadsad.chs.ass")),
                SourceFile(Path("dsadsad[CHS].ass")),
                SourceFile(Path("dsadsad_CHS.ass")),
                SourceFile(Path("[Shirokoi&Airota&VCB-Studio] Non Non Biyori [06][Ma10p_1080p][x265_flac].tc.ass")),
                SourceFile(Path("[SGS][Gundam00][05][BDRip][1920x1080][x264_FLACx2][GB_BIG5][9E442A43].sc.ass")),
                SourceFile(Path("[SGS][Gundam00][05][BDRip][1920x1080][x264_FLACx2][GB_BIG5][9E442A43].tc.ass")),
            )
        )
        val langs = sourceFiles.map {
            it.variables()["language"].toString()
        }
        assertContentEquals(listOf("zh-CHS", "zh-CHS", "zh-CHS", "zh-CHT", "zh-CHS", "zh-CHT"), langs)
    }

    @Test
    fun from_ass_format_file_content() {
        val files = listOf(
            SourceFile(languagePath.resolve("language-simple1.ass")),
            SourceFile(languagePath.resolve("language-simple2.ass")),
        )
        val result = LanguageVariableProvider(true).fileVariables(
            sourceItem(),
            PatternVariables.EMPTY,
            files
        )
        val p = files.zip(result)
            .associateBy({ (file, _) -> file.path.name }) { (_, vars) -> vars.variables()["language"] }
        val v1 = p.getValue("language-simple1.ass")
        assertEquals("zh-CHS", v1)

        val v2 = p.getValue("language-simple2.ass")
        assertEquals("zh-CHT", v2)
    }

    @Test
    fun from_srt_format_file_content() {
        val files = listOf(
            SourceFile(languagePath.resolve("language-simple1.srt")),
            SourceFile(languagePath.resolve("language-simple2.srt")),
        )
        val result = LanguageVariableProvider(true).fileVariables(
            sourceItem(),
            PatternVariables.EMPTY,
            files
        )
        val p = files.zip(result)
            .associateBy({ (file, _) -> file.path.name }) { (_, vars) -> vars.variables()["language"] }
        val v1 = p.getValue("language-simple1.srt")
        assertEquals("zh-CHS", v1)

        val v2 = p.getValue("language-simple2.srt")
        assertEquals("zh-CHT", v2)
    }
}