package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FixedFileContent
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
        "NCOP" to false,
        "[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [10][Ma10p_1080p][x265_flac].mkv" to true,
        "[SAIO-Raws] Karakai Jouzu no Takagi-san 2 - 01 [BD 1920x1080 HEVC-10bit OPUS ASSx2].mkv" to true,
        "[SAIO-Raws] Karakai Jouzu no Takagi-san 2 - 01 [BD 1920x1080 HEVC-10bit EDUS ASSx2].mkv" to true,
        "Menu (Vol.1).mkv" to false,
        "[ANK-Raws] Another Info05 (BDrip 1920x1080 HEVC-YUV420P10 FLAC)" to false,
        "[Yousei-raws] Sakamichi no Apollon (Episode 12 Preview) [BDrip 1920x1080 x264 FLAC]" to false,
        "Special/[ReinForce] Sekai de Ichiban Tsuyoku Naritai! - SP1 (BDRip 1920x1080 x264 FLAC)" to true,
        "[ReinForce] Sekai de Ichiban Tsuyoku Naritai! - Creditless ED2 (BDRip 1920x1080 x264 FLAC)" to false,
        "[ReinForce] Sekai de Ichiban Tsuyoku Naritai! - Creditless OP2 (BDRip 1920x1080 x264 FLAC)" to false,
    )

    @Test
    fun test() {
        for (datum in data) {
            val path = Path(datum.first)
            val fileContent = FixedFileContent(path)
            assertEquals(datum.second, AnimeFileFilter.test(fileContent), path.toString())
        }
    }
}