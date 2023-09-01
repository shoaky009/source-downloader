package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

class IncludingTargetPathsFileMover(
    private val fileMover: FileMover,
    private val storage: ProcessingStorage,
) : FileMover by fileMover {

    override fun exists(paths: List<Path>): List<Boolean> {
        val exists = fileMover.exists(paths)
        if (exists.all { it }) {
            return exists
        }
        return exists.zip(storage.targetPathExists(paths)) { a, b -> a || b }
    }

    override fun listPath(path: Path): List<Path> {
        val listPath = fileMover.listPath(path)
        return (listPath + storage.findSubPaths(path).map { it.targetPath }).distinct()
    }

}