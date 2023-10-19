package io.github.shoaky.sourcedownloader.sdk.util.masking

import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.junit.jupiter.api.Test

class MaskingTest {

    @Test
    fun masking() {
        val json = Jackson.toJsonString(Data("1111111", "22222"))
        assert(json.contains("11***11"))
        assert(json.contains("22222"))
    }

    private data class Data(
        @Masking
        val name: String,
        val mobile: String,
    )
}