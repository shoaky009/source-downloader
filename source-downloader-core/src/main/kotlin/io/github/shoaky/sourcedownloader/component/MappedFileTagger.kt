package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import kotlin.io.path.name

/**
 * 通过文件名(包括扩展名)映射来提供标签
 */
class MappedFileTagger(
    private val mapping: Map<String, String>
) : FileTagger {

    override fun tag(fileContent: SourceFile): String? {
        return mapping[fileContent.path.name]
    }
}