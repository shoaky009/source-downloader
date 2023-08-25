package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.setLastModifiedTime

/**
 * 更改对应Item文件夹的最后修改时间
 */
object TouchItemDirectory : RunAfterCompletion {

    override fun accept(t: ItemContent) {
        val filetime = FileTime.fromMillis(System.currentTimeMillis())
        t.sourceFiles
            .filter { it.fileSaveRootDirectory()?.exists() ?: false }
            .groupBy { it.fileSaveRootDirectory() }
            .mapNotNull { it.key }
            .forEach {
                log.debug("item:{} Touching directory: {}", t.sourceItem.title, it)
                it.setLastModifiedTime(filetime)
            }
    }
}

