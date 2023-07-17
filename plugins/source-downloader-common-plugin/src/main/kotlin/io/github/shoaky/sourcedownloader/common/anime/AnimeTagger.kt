package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import kotlin.io.path.nameWithoutExtension

/**
 * 先简单实现一个后续继续完善
 */
object AnimeTagger : FileTagger {

    private val sp = listOf("特别篇")

    override fun tag(fileContent: FileContent): String? {
        val filename = fileContent.fileDownloadPath.nameWithoutExtension
        val isSp = sp.any {
            filename.contains(it)
        }
        if (isSp) {
            return "special"
        }

        if (filename.contains("OVA")) {
            return "ova"
        }

        if (filename.contains("OAD")) {
            return "oad"
        }

        if (filename.contains("剧场版") || filename.contains("劇場版") || filename.contains("movie", ignoreCase = true)) {
            return "movie"
        }
        return null
    }
}