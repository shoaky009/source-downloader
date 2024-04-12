package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider

object FileSizeReplacementDecider : FileReplacementDecider {

    override fun isReplace(current: ItemContent, before: ItemContent?, existingFile: SourceFile): Boolean {
        val currentSize = getSize(current.fileContents.first()) ?: return false

        val existingFileSize = existingFile.attrs["size"]?.toString()?.toLong() ?: Long.MAX_VALUE
        return currentSize > existingFileSize
    }

    private fun getSize(file: FileContent?): Long? {
        val sizeAttr = file?.attrs?.get("size") ?: return null
        return sizeAttr.toString().toLong()
    }
}