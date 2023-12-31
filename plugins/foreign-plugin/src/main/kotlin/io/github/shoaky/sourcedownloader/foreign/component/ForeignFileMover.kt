package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

class ForeignFileMover(
    private val client: ForeignStateClient,
) : FileMover {

    override fun move(itemContent: ItemContent): Boolean {
        TODO("Not yet implemented")
    }

    override fun replace(itemContent: ItemContent): Boolean {
        TODO("Not yet implemented")
    }

    override fun exists(paths: List<Path>): List<Boolean> {
        return super.exists(paths)
    }

    override fun createDirectories(path: Path) {
        super.createDirectories(path)
    }

    override fun listPath(path: Path): List<Path> {
        return super.listPath(path)
    }

    override fun pathMetadata(path: Path): SourceFile {
        return super.pathMetadata(path)
    }
}