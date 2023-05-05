package xyz.shoaky.sourcedownloader.ai

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDateTime
import kotlin.io.path.Path

class OpenAiVariableProviderTest {

    @Test
    fun name() {
        val provider = OpenaiVariableProviderSupplier.apply(
            Properties.fromMap(
                mapOf(
                    "apiKeys" to listOf(""),
                    "resolveVariables" to listOf(
                        "episode:集", "season:季", "source:源例如BD", "language:语言编号RFC 4646"
                    )
                )
            )
        )

        val sourceGroup = provider.createSourceGroup(
            SourceItem("Karakai Jouzu no Takagi-san 2", URI("http://localhost"), LocalDateTime.now(), "", URI("http://localhost"))
        )

        val sourceFiles = sourceGroup.sourceFiles(
            listOf(Path("[UHA-WINGS&VCB-Studio] Karakai Jouzu no Takagi-san 2 [01][Ma10p_1080p][x265_flac].sc.ass"))
        ).first()
        println(sourceFiles.patternVariables().variables())

    }
}