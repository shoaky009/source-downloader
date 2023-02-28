package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.SourceFileContent
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.exists
import kotlin.io.path.moveTo

object MoveFile : FileMover {
    override fun rename(sourceFiles: List<SourceFileContent>, torrentHash: String?): Boolean {
        sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetFilePath())
            }
        return sourceFiles.all { it.targetFilePath().exists() }
    }
}

object MoveFileSupplier : ComponentSupplier<MoveFile> {
    override fun apply(props: ComponentProps): MoveFile {
        return MoveFile
    }

    override fun availableTypes(): List<ComponentType> {
        return listOf(
            ComponentType("move", FileMover::class)
        )
    }

}