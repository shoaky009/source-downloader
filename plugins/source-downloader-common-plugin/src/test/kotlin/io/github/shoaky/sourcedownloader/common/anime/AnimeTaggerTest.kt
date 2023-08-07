package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FixedFileContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class AnimeTaggerTest {

    @Test
    fun basic() {
        val f0 = FixedFileContent(fileDownloadPath = Path("[ANi] Dark Gathering -  黑暗集会 - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4].mp4"))

        assertEquals(null, AnimeTagger.tag(SourceFile(f0.fileDownloadPath)))
        val f1 = FixedFileContent(fileDownloadPath = Path("[爱恋字幕社&漫猫字幕社][剧场版关于我转生变成史莱姆这档事 红莲之绊篇][Tensei shitara Slime Datta Ken Movie: Guren no Kizuna-hen][1080p][MP4][简中]"))
        assertEquals("movie", AnimeTagger.tag(SourceFile(f1.fileDownloadPath)))
        val f2 = FixedFileContent(fileDownloadPath = Path("LoveLive! 虹咲学园学园偶像同好会 Next Sky OVA GB_CN HEVC10_opus 1080p [430.13 MB]"))
        assertEquals("ova", AnimeTagger.tag(SourceFile(f2.fileDownloadPath)))
        val f3 = FixedFileContent(fileDownloadPath = Path("LoveLive! 虹咲学园学园偶像同好会 Next Sky OAD GB_CN HEVC10_opus 1080p [430.13 MB]"))
        assertEquals("oad", AnimeTagger.tag(SourceFile(f3.fileDownloadPath)))
    }
}