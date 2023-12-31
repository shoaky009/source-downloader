package io.github.shoaky.sourcedownloader.foreign.component

import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

class ForeignFileExistsDetector(
    private val client: ForeignStateClient,
) : FileExistsDetector {

    override fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Path?> {
        TODO("Not yet implemented")
    }
}