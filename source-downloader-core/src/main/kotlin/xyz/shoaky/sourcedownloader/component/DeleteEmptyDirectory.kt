package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.Files
import kotlin.io.path.exists

object DeleteEmptyDirectory : RunAfterCompletion {
    override fun accept(t: SourceContent) {
        t.sourceFiles.firstOrNull()?.run {
            val directoryPath = this.itemDownloadRootDirectory()

            if (directoryPath != null && directoryPath.exists() && Files.list(directoryPath).count() == 0L) {
                Files.deleteIfExists(directoryPath)
            }
        }
    }
}