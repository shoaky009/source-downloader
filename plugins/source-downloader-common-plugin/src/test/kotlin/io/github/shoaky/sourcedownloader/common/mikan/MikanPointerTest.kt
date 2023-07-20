package io.github.shoaky.sourcedownloader.common.mikan

import io.github.shoaky.sourcedownloader.common.anime.MikanPointer
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MikanPointerTest {

    @Test
    fun clean() {
        val pointer = MikanPointer(
            shows = mutableMapOf(
                "NotCleaning" to LocalDateTime.now(),
                "Clean" to LocalDateTime.now().minusMonths(2L)
            )
        )
        pointer.cleanMonthly()
        assert(pointer.shows.containsKey("NotCleaning"))
        assert(pointer.shows.containsKey("Clean").not())
    }
}