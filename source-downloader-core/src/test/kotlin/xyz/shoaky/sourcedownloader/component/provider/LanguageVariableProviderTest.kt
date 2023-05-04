package xyz.shoaky.sourcedownloader.component.provider

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.LanguageVariableProviderSupplier
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path

class LanguageVariableProviderTest {

    @Test
    fun test() {
        val provider = LanguageVariableProviderSupplier.apply(
            Properties.empty()
        )

        val group = provider.createSourceGroup(sourceItem())
        val sourceFiles = group.sourceFiles(listOf(
            Path("dsadsad.chs.ass"),
            Path("dsadsad[CHS].ass"),
            Path("dsadsad_CHS.ass"),
        ))
        println(sourceFiles)
    }
}