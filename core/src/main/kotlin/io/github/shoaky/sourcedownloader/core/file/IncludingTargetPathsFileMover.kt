package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.apache.commons.collections4.trie.PatriciaTrie
import org.slf4j.LoggerFactory
import java.nio.file.Path

class IncludingTargetPathsFileMover(
    private val fileMover: FileMover,
    private val storage: ProcessingStorage,
) : FileMover by fileMover {

    // 单批次如果有太多的文件处理又慢的，可能会导致内存占用过高
    private val preoccupiedTargetPaths: PatriciaTrie<Path> = PatriciaTrie<Path>()

    override fun exists(paths: List<Path>): List<Boolean> {
        val exists = fileMover.exists(paths)
        if (exists.all { it }) {
            return exists
        }

        val zip = exists.zip(storage.targetPathExists(paths)) { a, b -> a || b }
        synchronized(preoccupiedTargetPaths) {
            return paths.mapIndexed { index, path ->
                // 如果不存在的需要再检查预占用的有没有
                if (zip[index].not()) {
                    // 处理并行处理未预占用的重复下载的问题
                    preoccupiedTargetPaths.contains(path.toString())
                } else {
                    true
                }
            }
        }
    }

    override fun listPath(path: Path): List<Path> {
        val paths = preoccupiedTargetPaths.prefixMap(path.parent.toString()).values
        val listPath = fileMover.listPath(path)
        return (listPath + storage.findSubPaths(path).map { it.targetPath } + paths).distinct()
    }

    fun preoccupiedTargetPath(paths: Collection<Path>) {
        log.debug("PreoccupiedTargetPath: {}", paths)
        synchronized(preoccupiedTargetPaths) {
            preoccupiedTargetPaths.putAll(
                paths.map { it.toString() to it }
            )
        }

        if (log.isDebugEnabled) {
            log.debug("Preoccupied target path, current preoccupied target paths: {}", preoccupiedTargetPaths)
        }
    }

    @Synchronized
    fun releaseAll() {
        synchronized(preoccupiedTargetPaths) {
            preoccupiedTargetPaths.clear()
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(IncludingTargetPathsFileMover::class.java)
    }
}