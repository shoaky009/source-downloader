package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

interface FileContent {

    /**
     * 保存文件的根目录
     */
    val downloadPath: Path

    /**
     * 文件全路径
     */
    val fileDownloadPath: Path
    val patternVariables: PatternVariables

    fun targetPath(): Path

    fun saveDirectoryPath(): Path {
        return targetPath().parent
    }

    /**
     * 获取item文件对应的顶级目录e.g. 文件保存在下/mnt/bangumi/FATE/Season 01 返回 /mnt/bangumi/FATE
     * Returns:
     * null如果item的文件是保存在saveRootPath下
     */
    fun saveItemFileRootDirectory(): Path?

    /**
     * 获取item文件对应的顶级目录e.g. 文件保存在下/downloads/FATE/Season 01 返回 /downloads/FATE/
     * Returns:
     * null如果item的文件是保存在downloadPath下
     */
    fun downloadItemFileRootDirectory(): Path?
}