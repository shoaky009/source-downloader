package io.github.shoaky.sourcedownloader.component.replacer

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FullWidthReplacerTest {

    @Test
    fun test() {
        val str = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ"
        val replaced = FullWidthReplacer.replace("", str)
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", replaced)
    }
}