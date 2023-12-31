package io.github.shoaky.sourcedownloader.sdk

import io.github.shoaky.sourcedownloader.sdk.component.FileReplacementDecider
import java.net.URI
import java.nio.file.Path

interface FileContent {

    /**
     * The root directory of the download file. e.g. /mnt/downloads
     */
    val downloadPath: Path

    /**
     * The full path of the file. e.g. /mnt/downloads/test.txt
     */
    val fileDownloadPath: Path

    val patternVariables: PatternVariables

    /**
     * The tags of the file.
     */
    val tags: Set<String>

    /**
     * The attributes of the file.
     */
    val attrs: Map<String, Any>

    /**
     * The URI of the file.
     */
    val fileUri: URI?

    /**
     * Exist target path of the file, is not the same as target path, depend on the [FileReplacementDecider].
     */
    val existTargetPath: Path?

    /**
     * @return The target path of the file. e.g. /mnt/save/2023-01-01/test.txt
     */
    fun targetPath(): Path

    /**
     * @return The target directory of the file. e.g. /mnt/save/2023-01-01
     */
    fun saveDirectoryPath(): Path {
        return targetPath().parent
    }

    /**
     * @return The status of the file.
     */
    fun status(): FileStatus

    /**
     * example:
     *  savePath=/mnt/bangumi
     *  targetPath=/mnt/bangumi/FATE/Season 01/EP01.mp4
     *  return /mnt/bangumi/FATE
     *
     *  savePath=/mnt/demo
     *  targetPath=/mnt/demo/test.txt
     *  return null
     *
     * @return The target directory of the file, when the path is equal to savePath, return null.
     */
    fun fileSaveRootDirectory(): Path?

    /**
     * example:
     *  downloadPath=/downloads
     *  targetPath=/downloads/FATE/Season 01/EP01.mp4
     *  return /downloads/FATE
     *
     *  downloadPath=/downloads
     *  targetPath=/downloads/test.txt
     *  return null
     *
     * @return The download directory of the file, when the path is equal to downloadPath, return null.
     */
    fun fileDownloadRootDirectory(): Path? {
        if (fileDownloadPath.parent == downloadPath) {
            return null
        }

        var path = fileDownloadPath.parent
        while (path.parent != downloadPath) {
            path = path.parent
        }
        return path
    }

    /**
     * example:
     *  fileDownloadPath=/mnt/bangumi/FATE/Season 01/EP01.mp4
     *  return FATE/Season 01
     *
     * @return The relative directory of the file.
     */
    fun fileDownloadRelativeParentDirectory(): Path? {
        val relativize = downloadPath.relativize(fileDownloadPath)
        return relativize.parent
    }
}