package xyz.shoaky.sourcedownloader.tagger

import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes
import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.FileContent
import xyz.shoaky.sourcedownloader.sdk.component.FileTagger
import kotlin.io.path.name

internal object SimpleFileTagger : FileTagger {

    private val log = LoggerFactory.getLogger(SimpleFileTagger::class.java)
    private val tika = Tika()

    override fun tag(fileContent: FileContent): String? {
        val name = fileContent.fileDownloadPath.name
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
