package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import kotlin.io.path.name

class MappedFileTagger(
    private val mapping: Map<String, String>
) : FileTagger {

    override fun tag(fileContent: SourceFile): String? {
        return mapping[fileContent.path.name]
    }
}