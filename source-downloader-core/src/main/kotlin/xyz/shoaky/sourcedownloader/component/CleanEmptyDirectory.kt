package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

object CleanEmptyDirectory : RunAfterCompletion {
    @OptIn(ExperimentalPathApi::class)
    override fun accept(t: SourceContent) {
        t.sourceFiles.firstOrNull()?.run {
            val directoryPath = this.itemDownloadRootDirectory()
            if (directoryPath != null && Files.walk(directoryPath).allMatch(Files::isDirectory)) {
                directoryPath.deleteRecursively()
            }
        }
    }
}