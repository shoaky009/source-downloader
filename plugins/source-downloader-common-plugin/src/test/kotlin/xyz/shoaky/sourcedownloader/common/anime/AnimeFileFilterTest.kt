package xyz.shoaky.sourcedownloader.common.anime

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class AnimeFileFilterTest {

    private val data = listOf(
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [PV02][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [NCOP][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [NCED_EP12][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [Menu06][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [Blu-ray & DVD Selling CM][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [Blu-ray & DVD Selling CM][Ma10p_1080p][x265_flac].mkv" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [10][Ma10p_1080p][x265_flac].mkv" to true,
    )

    @Test
    fun test() {
        for (datum in data) {
            val path = Path(datum.first)
            assertEquals(datum.second, AnimeFileFilter.test(path))
        }
    }
}