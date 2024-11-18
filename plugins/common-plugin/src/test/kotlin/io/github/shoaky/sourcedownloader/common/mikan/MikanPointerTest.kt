package io.github.shoaky.sourcedownloader.common.mikan

import io.github.shoaky.sourcedownloader.common.anime.MikanPointer
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class MikanPointerTest {

    @Test
    fun clean() {
        val pointer = MikanPointer(
            shows = mutableMapOf(
                "NotCleaning" to OffsetDateTime.now(),
                "Clean" to OffsetDateTime.now().minusMonths(2L)
            )
        )
        pointer.cleanMonthly()
        assert(pointer.shows.containsKey("NotCleaning"))
        assert(pointer.shows.containsKey("Clean").not())
    }
}