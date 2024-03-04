package io.github.shoaky.sourcedownloader.common.tagger

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes
import org.slf4j.LoggerFactory
import kotlin.io.path.name

/**
 * 从扩展名中提取文件类型
 */
class SimpleFileTagger(
    private val externalMapping: Map<String, String> = emptyMap()
) : FileTagger {

    private val log = LoggerFactory.getLogger(SimpleFileTagger::class.java)
    private val tika = Tika()
    private val mapping: Map<String, String> = buildMap {
        val defaultMapping: Map<String, String> = mapOf(
            "x-subrip" to "subtitle"
        )
        this.putAll(defaultMapping)
        // 外部映射优先级更高
        this.putAll(externalMapping)
    }

    override fun tag(sourceFile: SourceFile): String? {
        val name = sourceFile.path.name
        if (name.lastIndexOf(".") < 0) {
            return null
        }
        return tag(name)
    }

    fun tag(name: String): String? {
        val detect = tika.detect(name)
        if (detect != MimeTypes.OCTET_STREAM) {
            val split = detect.split("/")
            val type = split.first()
            if (type != "application") {
                return type
            }
            return mapping[split.last()]
        }
        log.debug("Can't detect file type for {}", name)
        return null
    }

}
