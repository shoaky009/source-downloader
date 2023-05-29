package xyz.shoaky.sourcedownloader.component.provider

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.LanguageVariableProviderSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SourceFile
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path

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
        println(sourceFiles)
    }
}