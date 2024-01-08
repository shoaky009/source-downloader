package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*

class IncludingTargetPathsFileMover(
    private val fileMover: FileMover,
    private val storage: ProcessingStorage,
) : FileMover by fileMover {

    private val preoccupiedTargetPaths: MutableSet<Path> = Collections.synchronizedSet(mutableSetOf())

    override fun exists(paths: List<Path>): List<Boolean> {
        val exists = fileMover.exists(paths)
        if (exists.all { it }) {
            return exists
        }

        val zip = exists.zip(storage.targetPathExists(paths)) { a, b -> a || b }
        return paths.mapIndexed { index, path ->
            // 如果不存在的需要再检查预占用的有没有
            if (zip[index].not()) {
                // 处理并行处理未预占用的重复下载的问题
                preoccupiedTargetPaths.contains(path)
            } else {
                true
            }
        }

    }

    override fun listPath(path: Path): List<Path> {
        val listPath = fileMover.listPath(path)
        return (listPath + storage.findSubPaths(path).map { it.targetPath }).distinct()
    }

    fun preoccupiedTargetPath(paths: Collection<Path>) {
        log.info("preoccupiedTargetPath: {}", paths)
        preoccupiedTargetPaths.addAll(paths)
        if (log.isDebugEnabled) {
            log.debug("Preoccupied target path, current preoccupied target paths: {}", preoccupiedTargetPaths)
        }
    }

    fun releasePreoccupiedTargetPath(paths: Collection<Path>) {
        log.info("ReleasePreoccupiedTargetPath: {}", paths)
        preoccupiedTargetPaths.removeAll(paths.toSet())
        if (log.isDebugEnabled) {
            log.debug("Release preoccupied target path, current preoccupied target paths: {}", preoccupiedTargetPaths)
        }
    }

    fun releaseAll() {
        preoccupiedTargetPaths.clear()
    }

    fun currentOccupiedTargetPaths(): Set<Path> {
        return preoccupiedTargetPaths.toSet()
    }

    companion object {

        private val log = LoggerFactory.getLogger(IncludingTargetPathsFileMover::class.java)
    }
}