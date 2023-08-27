package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.supplier.LanguageVariableProviderSupplier
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
            Properties.EMPTY
        )

        val group = provider.createSourceGroup(sourceItem())
        val sourceFiles = group.filePatternVariables(listOf(
            SourceFile(Path("dsadsad.chs.ass")),
            SourceFile(Path("dsadsad[CHS].ass")),
            SourceFile(Path("dsadsad_CHS.ass")),
        ))
        val langs = sourceFiles.map {
            it.patternVariables().variables()["language"].toString()
        }
        assertContentEquals(listOf("zh-CHS", "zh-CHS", "zh-CHS"), langs)
    }
}