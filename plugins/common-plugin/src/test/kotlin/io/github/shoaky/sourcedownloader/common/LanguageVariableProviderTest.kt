package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.supplier.LanguageVariableProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertContentEquals

class LanguageVariableProviderTest {

    @Test
    fun test() {
        val provider = LanguageVariableProviderSupplier.apply(
            CoreContext.empty,
            Properties.empty
        )

        val item = sourceItem()
        val group = provider.itemSharedVariables(item)

        val sourceFiles = provider.itemFileVariables(
            item, group, listOf(
                SourceFile(Path("dsadsad.chs.ass")),
                SourceFile(Path("dsadsad[CHS].ass")),
                SourceFile(Path("dsadsad_CHS.ass")),
                SourceFile(Path("[Shirokoi&Airota&VCB-Studio] Non Non Biyori [06][Ma10p_1080p][x265_flac].tc.ass")),
            )
        )
        val langs = sourceFiles.map {
            it.variables()["language"].toString()
        }
        assertContentEquals(listOf("zh-CHS", "zh-CHS", "zh-CHS", "zh-CHT"), langs)
    }
}