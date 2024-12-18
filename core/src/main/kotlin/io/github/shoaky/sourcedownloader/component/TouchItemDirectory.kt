package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener
import org.slf4j.LoggerFactory
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.setLastModifiedTime

/**
 * 更改对应Item文件夹的最后修改时间
 */
object TouchItemDirectory : ProcessListener {

    override fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {
        val filetime = FileTime.fromMillis(System.currentTimeMillis())
        itemContent.fileContents
            .filter { it.fileSaveRootDirectory()?.exists() ?: false }
            .groupBy { it.fileSaveRootDirectory() }
            .mapNotNull { it.key }
            .forEach {
                log.debug("item:{} Touching directory: {}", itemContent.sourceItem.title, it)
                it.setLastModifiedTime(filetime)
            }
    }

    private val log = LoggerFactory.getLogger(TouchItemDirectory::class.java)
}

