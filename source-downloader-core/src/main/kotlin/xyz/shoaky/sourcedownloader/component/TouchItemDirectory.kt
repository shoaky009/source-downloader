package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
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
                log.debug("item:${t.sourceItem.title} Touching directory: $it")
                it.setLastModifiedTime(filetime)
            }
    }
}

object TouchItemDirectorySupplier : SdComponentSupplier<TouchItemDirectory> {
    override fun apply(props: ComponentProps): TouchItemDirectory {
        return TouchItemDirectory
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.run("touchItemDirectory"))
    }

    override fun getComponentClass(): Class<TouchItemDirectory> {
        return TouchItemDirectory::class.java
    }

}