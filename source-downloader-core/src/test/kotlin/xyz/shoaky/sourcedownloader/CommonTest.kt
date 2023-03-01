package xyz.shoaky.sourcedownloader

import com.google.common.base.CaseFormat
import org.junit.jupiter.api.Test
import org.springframework.util.StreamUtils
import xyz.shoaky.sourcedownloader.core.component.QbittorrentDownloader
import xyz.shoaky.sourcedownloader.sdk.component.SdComponent
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.reflect.full.allSuperclasses

//@Disabled("实验")
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
    fun name1() {
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
    fun dsa() {
        val path = Path("file:///Users/shoaky/temp/downloads/%5BANi%5D%20%E5%89%8D%E9%80%B2%E5%90%A7%EF%BC%81%E7%99%BB%E5%B1%B1%E5%B0%91%E5%A5%B3%20%20Next%20Summit%EF%BC%88%E5%83%85%E9%99%90%E6%B8%AF%E6%BE%B3%E5%8F%B0%E5%9C%B0%E5%8D%80%EF%BC%89%20-%2001%20%5B1080P%5D%5BBilibili%5D%5BWEB-DL%5D%5BAAC%20AVC%5D%5BCHT%20CHS%5D.mp4")
        println(path.exists())
        println(path)
    }
}