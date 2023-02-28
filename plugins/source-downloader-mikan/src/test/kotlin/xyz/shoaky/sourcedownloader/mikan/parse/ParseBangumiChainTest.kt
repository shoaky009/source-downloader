package xyz.shoaky.sourcedownloader.mikan.parse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParseBangumiChainTest {
    @Test
    fun should_get_episode8() {
        val chain = ParseChain()
        val apply = chain.apply(create("前进吧！登山少女"),
            "[ANi]  前进吧！登山少女  Next Summit（仅限港澳台地区） - 08 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS][MP4]")
        assertEquals(8, apply.episode)
    }

    @Test
    fun should_get_season1() {
        val chain = ParseChain()
        val apply = chain.apply(create("4个人各自有着自己的秘密"),
            "[ANi]  前进吧！登山少女  Next Summit（仅限港澳台地区） - 08 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS][MP4]")
        assertEquals(1, apply.season)
    }

    @Test
    fun should_get_season3() {
        val chain = ParseChain()
        val apply = chain.apply(create("入间同学入魔了 第三季"),
            "[ANi]  入间同学入魔了 第三季 - 04 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS][MP4]")
        assertEquals(3, apply.season)
    }

    @Test
    fun should_get_season1_test2() {
        val chain = ParseChain()
        val apply = chain.apply(create("入间同学入魔了 第三季"),
            "[ANi]  入间同学入魔了 第三季 - 04 [1080P][Bilibili][WEB-DL][AAC AVC][CHT CHS][MP4]")
        assertEquals(3, apply.season)
    }

    @Test
    fun should_get_season2() {
        val chain = ParseChain()
        val apply = chain.apply(create("魔王学院的不适任者～史上最强的魔王始祖，转生就读子孙们的学校～第二季"),
            "[ANi] 魔王学院的不适任者～史上最强的魔王始祖，转生就读子孙们的学校～第二季 - 02 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]")
        assertEquals(2, apply.season)
    }

    //TODO
    // 妖幻三重奏
    // /mnt/test/宇崎学妹想要玩！ω/Season 1
    // /mnt/test/间谍过家家 第二部分/Season 2
    // [ANi] 魔王学院的不适任者～史上最强的魔王始祖，转生就读子孙们的学校～第二季 - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]

    //    <item>
    //    <guid isPermaLink="false">[织梦字幕组][夏日重现 Summer Time Rendering][BD-RIP][下卷][13-25集][1080P][AVC][简日双语]</guid>
    //    <link>https://mikanani.me/Home/Episode/56c134bff5b56d6f3163e688b34ff781ba7cb6d9</link>
    //    <title>[织梦字幕组][夏日重现 Summer Time Rendering][BD-RIP][下卷][13-25集][1080P][AVC][简日双语]</title>
    //    <description>[织梦字幕组][夏日重现 Summer Time Rendering][BD-RIP][下卷][13-25集][1080P][AVC][简日双语][7.76 GB]</description>
    //    <torrent xmlns="https://mikanani.me/0.1/">
    //    <link>https://mikanani.me/Home/Episode/56c134bff5b56d6f3163e688b34ff781ba7cb6d9</link>
    //    <contentLength>8332236800</contentLength>
    //    <pubDate>2022-12-27T13:45:29.62</pubDate>
    //    </torrent>
    //    <enclosure type="application/x-bittorrent" length="8332236800" url="https://mikanani.me/Download/20221227/56c134bff5b56d6f3163e688b34ff781ba7cb6d9.torrent"/>
    //    </item>
}