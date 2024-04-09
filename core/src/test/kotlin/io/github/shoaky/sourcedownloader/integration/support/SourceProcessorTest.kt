package io.github.shoaky.sourcedownloader.integration.support

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider
import kotlin.io.path.nameWithoutExtension

object Item2ReplaceDecider : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean {
        return current.sourceFiles.any { it.fileDownloadPath.nameWithoutExtension == "test-replace2" }
    }

}

