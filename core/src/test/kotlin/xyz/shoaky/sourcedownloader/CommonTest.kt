package xyz.shoaky.sourcedownloader

import com.google.common.base.CaseFormat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.util.StreamUtils
import org.springframework.web.util.UriComponentsBuilder
import xyz.shoaky.sourcedownloader.core.component.QbittorrentDownloader
import xyz.shoaky.sourcedownloader.sdk.component.*
import java.util.*
import kotlin.reflect.full.allSuperclasses

@Disabled("实验")
class CommonTest {

    @Test
    fun script() {
        val processBuilder = ProcessBuilder("/Users/shoaky/temp/aaa.sh", "var1", "var0")
        val process = processBuilder.start()
        println(StreamUtils.copyToString(process.inputStream, Charsets.UTF_8))
    }

    @Test
    fun path() {
        val sealedSubclasses = SdComponent::class.sealedSubclasses
        val map = SdComponent::class.sealedSubclasses
            .mapNotNull { it.simpleName }
            .map { CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, it) }
        println(map)
        val supertypes = QbittorrentDownloader::class.allSuperclasses
            .filter { sealedSubclasses.contains(it) }
            .associateBy { klass ->
                klass
            }
        println(supertypes)
    }

    @Test
    fun name() {
        val inputList: List<Map<String, String>> = listOf(
            mapOf("key1" to "value1-1", "key2" to "value2-1"),
            mapOf("key1" to "value1-2", "key2" to "value2-2"),
            mapOf("key1" to "value1-1", "key2" to "value2-3")
        )

        val outputMap: Map<String, List<String>> = inputList
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
        println(outputMap)
        println(outputMap.values.flatten())
    }

    @Test
    fun ddd() {
        val build = UriComponentsBuilder.fromHttpUrl("http://bgm.tv/subject/290980").build()
        println(build.pathSegments.last())

    }
}