package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WindowsPathReplacerTest {

    @Test
    fun test() {
        val replace = WindowsPathReplacer.replace("", "<>:\\/|?*1123adsad")
        assertEquals("＜＞：＼／｜？＊1123adsad", replace)
    }
}