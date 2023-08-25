package io.github.shoaky.sourcedownloader.common.anime

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import kotlin.io.path.nameWithoutExtension

/**
 * 对动画常见的特别篇进行标记，规则如下
 * 特别篇:special
 * OVA:ova
 * OAD:oad
 * 剧场版,劇場版,movie:movie
 */
object AnimeTagger : FileTagger {

    private val sp = listOf("特别篇")

    override fun tag(fileContent: SourceFile): String? {
        val filename = fileContent.path.nameWithoutExtension
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