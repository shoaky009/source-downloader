package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.setLastModifiedTime

object TouchItemDirectory : RunAfterCompletion {
    override fun accept(t: SourceContent) {
        val filetime = FileTime.fromMillis(System.currentTimeMillis())
        t.sourceFiles
            .filter { it.itemFileRootDirectory()?.exists() ?: false }
            .groupBy { it.itemFileRootDirectory() }
            .mapNotNull { it.key }
            .forEach {
                log.debug("item:{} Touching directory: {}", t.sourceItem.title, it)
                it.setLastModifiedTime(filetime)
            }
    }
}

