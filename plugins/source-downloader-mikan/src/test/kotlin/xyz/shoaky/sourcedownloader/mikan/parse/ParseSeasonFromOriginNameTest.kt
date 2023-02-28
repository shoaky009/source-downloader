package xyz.shoaky.sourcedownloader.mikan.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.api.bangumi.Subject
import java.time.LocalDate

fun create(name: String): SubjectContent {
    val subject = Subject(1, name, name, LocalDate.now(), 12)
    return SubjectContent(subject, name)
}

class ParseSeasonFromOriginNameTest {

    private val parser = ParseSeasonFromOriginName()

    @Test
    fun parse_number_season() {
        assertEquals(1, parser.apply(create("我的 第1期"), "").season)
        assertEquals(2, parser.apply(create("我的 第2期"), "").season)
        assertEquals(3, parser.apply(create("我的 第3期"), "").season)
        assertEquals(4, parser.apply(create("我的 第4期"), "").season)
        assertEquals(5, parser.apply(create("我的 第5期"), "").season)
        assertEquals(10, parser.apply(create("我的 第10期"), "").season)
        assertEquals(15, parser.apply(create("我的 第15期"), "").season)
    }

    @Test
    fun parse_chinese_number_season() {
        assertEquals(1, parser.apply(create("我的 第一期"), "").season)
        assertEquals(2, parser.apply(create("我的 第二期"), "").season)
        assertEquals(3, parser.apply(create("我的 第三期"), "").season)
        assertEquals(10, parser.apply(create("我的 第十期"), "").season)
    }

    @Test
    fun parse_normal_name() {

        assertEquals(1, parser.apply(create("期待在地下城邂逅有错吗"), "").season)
    }

    @Test
    fun parse_blank_name() {
        assertEquals(1, parser.apply(create("机动战士钢弹 水星的魔女"), "").season)
    }

    @Test
    fun parse_roman_numerals() {
        assertEquals(1, parser.apply(create("期待在地下城邂逅有错吗"), "").season)
        assertEquals(2, parser.apply(create("期待在地下城邂逅有错吗Ⅱ"), "").season)
        assertEquals(3, parser.apply(create("期待在地下城邂逅有错吗Ⅲ"), "").season)
        assertEquals(4, parser.apply(create("期待在地下城邂逅有错吗 Ⅳ 深章 灾厄篇"), "").season)
        assertEquals(4, parser.apply(create("期待在地下城邂逅有错吗 Ⅳ 新章 迷宫篇"), "").season)
        assertEquals(2, parser.apply(create("魔王学院的不适任者～史上最强的魔王始祖，转生就读子孙们的学校～ Ⅱ"), "").season)
    }
}