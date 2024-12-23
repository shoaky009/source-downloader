package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

class ReadonlyFileMover(fileMover: FileMover) : FileMover by fileMover {

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        throw UnsupportedOperationException("ReadonlyFileMover")
    }

    override fun createDirectories(path: Path) {
        throw UnsupportedOperationException("ReadonlyFileMover")
    }

    override fun replace(itemContent: ItemContent): Boolean {
        throw UnsupportedOperationException("ReadonlyFileMover")
    }
}