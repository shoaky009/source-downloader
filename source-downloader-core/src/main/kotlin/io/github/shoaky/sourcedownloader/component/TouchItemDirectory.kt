package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.setLastModifiedTime

object TouchItemDirectory : RunAfterCompletion {
    override fun accept(t: SourceContent) {
        val filetime = FileTime.fromMillis(System.currentTimeMillis())
        t.sourceFiles
            .filter { it.itemSaveRootDirectory()?.exists() ?: false }
            .groupBy { it.itemSaveRootDirectory() }
            .mapNotNull { it.key }
            .forEach {
                log.debug("item:{} Touching directory: {}", t.sourceItem.title, it)
                it.setLastModifiedTime(filetime)
            }
    }
}

