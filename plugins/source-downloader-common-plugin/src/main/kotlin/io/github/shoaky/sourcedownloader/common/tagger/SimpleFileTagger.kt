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
object SimpleFileTagger : FileTagger {

    private val log = LoggerFactory.getLogger(SimpleFileTagger::class.java)
    private val tika = Tika()

    override fun tag(fileContent: SourceFile): String? {
        val name = fileContent.path.name
        if (name.lastIndexOf(".") < 0) {
            return null
        }
        return tag(name)
    }

    fun tag(name: String): String? {
        val detect = tika.detect(name)
        if (detect != MimeTypes.OCTET_STREAM) {
            return detect.split("/").first()
        }
        log.info("Can't detect file type for $name")
        return null
    }

}
