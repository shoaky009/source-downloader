package io.github.shoaky.sourcedownloader.common.anitom

import io.github.shoaky.sourcedownloader.common.supplier.AnitomVariableProviderSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class AnitomVariableProviderTest {

    @Test
    fun test() {
        val provider = AnitomVariableProviderSupplier.apply(CoreContext.empty, Properties.empty)
        val sourceItem =
            sourceItem()
        val group = provider.createSourceGroup(sourceItem)
        assertEquals(0, group.sharedPatternVariables().variables().size)

        val files = group.filePatternVariables(
            listOf(SourceFile(Path("[漫猫字幕社&波子汽水汉化组][在地下城寻求邂逅是否搞错了什么 IV 深章 灾厄篇][Dungeon ni Deai wo Motomeru no wa Machigatteiru Darou ka S4][20][720P][MP4][繁中].mp4")))
        )

        assert(files.first().patternVariables().variables().isNotEmpty())
    }
}