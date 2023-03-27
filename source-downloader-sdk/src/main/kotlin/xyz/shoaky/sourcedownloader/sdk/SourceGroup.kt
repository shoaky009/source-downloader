package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface SourceGroup {

    /**
     * @return 源文件列表
     */
    fun sourceFiles(paths: List<Path>): List<SourceFile>

}