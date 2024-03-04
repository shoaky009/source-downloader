package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.component.replacer.WindowsPathReplacer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WindowsPathReplacerTest {

    @Test
    fun test() {
        val replace = WindowsPathReplacer.replace("", "<>:\\/|?*1123adsad")
        assertEquals("＜＞：＼／｜？＊1123adsad", replace)
    }
}