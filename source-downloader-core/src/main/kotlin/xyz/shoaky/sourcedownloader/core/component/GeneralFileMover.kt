package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.SourceFileContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import kotlin.io.path.exists
import kotlin.io.path.moveTo

object GeneralFileMover : FileMover {
    override fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String?): Boolean {
        sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetFilePath())
            }
        return sourceFiles.all { it.targetFilePath().exists() }
    }
}

object MoveFileSupplier : SdComponentSupplier<GeneralFileMover> {
    override fun apply(props: ComponentProps): GeneralFileMover {
        return GeneralFileMover
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("general", FileMover::class)
        )
    }

}