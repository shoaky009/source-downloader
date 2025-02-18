package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sourceItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class AnimeFileReplacerTest {

    @Test
    fun given_normal_and_version() {
        val content1 =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][Hi10p_1080p][x264_flac].mkv"),
                emptyList()
            )
        assertEquals(
            false, AnimeReplacementDecider.isReplace(
                content1, null, SourceFile(Path(""))
            )
        )

        val content2 =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][v2][Hi10p_1080p][x264_flac].mkv"),
                emptyList()
            )
        assertEquals(
            true, AnimeReplacementDecider.isReplace(
                content2, null, SourceFile(Path(""))
            )
        )
    }

    @Test
    fun given_bilibili_and_version() {
        val current =
            FixedItemContent(
                sourceItem(title = "[ANi] 因为不是真正的伙伴而被逐出勇者队伍，流落到边境展开慢活人生 第二季（仅限港澳台地区） - 01 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS][V2][MP4]"),
                emptyList()
            )
        assertEquals(
            true, AnimeReplacementDecider.isReplace(
                current, null, SourceFile(Path(""))
            )
        )

        val notBillibili = FixedItemContent(
            sourceItem(title = "[ANi] 因为不是真正的伙伴而被逐出勇者队伍，流落到边境展开慢活人生 第二季 - 01 [1080P][WEB-DL][AAC AVC][CHT CHS][MP4]"),
            emptyList()
        )
        assertEquals(
            false, AnimeReplacementDecider.isReplace(
                current, notBillibili, SourceFile(Path(""))
            )
        )
    }

    @Test
    fun given_current_and_before_version() {
        val current =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02V2].mkv"),
                emptyList()
            )
        val before =
            FixedItemContent(
                sourceItem(title = "[DMG&VCB-S][Saki Zenkoku Hen][02][v3].mkv"),
                emptyList()
            )
        assertEquals(
            false,
            AnimeReplacementDecider.isReplace(current, before, SourceFile(Path("")))
        )

        assertEquals(
            true,
            AnimeReplacementDecider.isReplace(before, current, SourceFile(Path("")))
        )
    }
}