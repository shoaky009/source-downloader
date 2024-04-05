package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.apache.commons.collections4.trie.PatriciaTrie
import org.slf4j.LoggerFactory
import java.nio.file.Path

class IncludingTargetPathsFileMover(
    private val fileMover: FileMover,
    private val storage: ProcessingStorage,
) : FileMover by fileMover {

    private val preoccupiedTargetPaths: PatriciaTrie<ItemPath> = PatriciaTrie<ItemPath>()

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
        val paths = preoccupiedTargetPaths.prefixMap(path.parent.toString()).values.map { it.path }
        val listPath = fileMover.listPath(path)
        return (listPath + storage.findSubPaths(path).map { it.targetPath } + paths).distinct()
    }

    fun preoccupiedTargetPath(sourceItem: SourceItem, paths: Collection<Path>) {
        log.debug("PreoccupiedTargetPath: {}", paths)
        if (paths.isEmpty()) {
            return
        }
        synchronized(preoccupiedTargetPaths) {
            preoccupiedTargetPaths.putAll(
                paths.map { it.toString() to ItemPath(sourceItem, it) }
            )
        }

        if (log.isDebugEnabled) {
            log.debug("Preoccupied target path, current preoccupied target paths: {}", preoccupiedTargetPaths)
        }
    }

    fun releaseAll() {
        synchronized(preoccupiedTargetPaths) {
            preoccupiedTargetPaths.clear()
        }
    }

    fun release(sourceItem: SourceItem, paths: Collection<Path>) {
        if (paths.isEmpty()) {
            return
        }
        synchronized(preoccupiedTargetPaths) {
            paths.forEach {
                val pathString = it.toString()
                val value = preoccupiedTargetPaths[pathString]
                if (value != null && value.sourceItem == sourceItem) {
                    preoccupiedTargetPaths.remove(pathString)
                }
            }
        }
    }

    fun release(sourceItem: SourceItem) {
        synchronized(preoccupiedTargetPaths) {
            preoccupiedTargetPaths.entries.removeIf { it.value.sourceItem == sourceItem }
        }
    }

    private data class ItemPath(
        val sourceItem: SourceItem,
        // 通过prefix查找时获取对应的路径
        val path: Path,
    )

    companion object {

        private val log = LoggerFactory.getLogger(IncludingTargetPathsFileMover::class.java)
    }
}