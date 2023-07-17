package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.TestFileContent
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class AnimeTaggerTest {

    @Test
    fun basic() {
        val f0 = TestFileContent(fileDownloadPath = Path("[ANi] Dark Gathering -  黑暗集会 - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4].mp4"))
        assertEquals(null, AnimeTagger.tag(f0))

        val f1 = TestFileContent(fileDownloadPath = Path("[爱恋字幕社&漫猫字幕社][剧场版关于我转生变成史莱姆这档事 红莲之绊篇][Tensei shitara Slime Datta Ken Movie: Guren no Kizuna-hen][1080p][MP4][简中]"))
        assertEquals("movie", AnimeTagger.tag(f1))

        val f2 = TestFileContent(fileDownloadPath = Path("LoveLive! 虹咲学园学园偶像同好会 Next Sky OVA GB_CN HEVC10_opus 1080p [430.13 MB]"))
        assertEquals("ova", AnimeTagger.tag(f2))

        val f3 = TestFileContent(fileDownloadPath = Path("LoveLive! 虹咲学园学园偶像同好会 Next Sky OAD GB_CN HEVC10_opus 1080p [430.13 MB]"))
        assertEquals("oad", AnimeTagger.tag(f3))
    }
}