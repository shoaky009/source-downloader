package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

class IncludingTargetPathsFileMover(
    private val storage: ProcessingStorage,
    private val fileMover: FileMover
) : FileMover by fileMover {

    override fun exists(paths: List<Path>): Boolean {
        if (fileMover.exists(paths)) {
            return true
        }
        return storage.targetPathExists(paths).all { it }
    }

    override fun listPath(path: Path): List<Path> {
        val listPath = fileMover.listPath(path)
        return (listPath + storage.findSubPaths(path).map { it.targetPath }).distinct()
    }

}