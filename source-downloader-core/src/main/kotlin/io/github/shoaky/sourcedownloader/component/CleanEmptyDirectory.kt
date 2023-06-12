package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
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