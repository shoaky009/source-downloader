package xyz.shoaky.sourcedownloader.sdk

import java.nio.file.Path

class PatternVars {
    constructor()
    constructor(vars: Map<String, String>) {
        this.vars.putAll(vars)
    }

    private val vars = mutableMapOf<String, String>()

    fun addVar(name: String, value: String) {
        vars[name] = value
    }

    fun addVar(name: String, value: Number) {
        vars[name] = value.toString()
    }

    fun getVar(name: String): String? {
        return vars[name]
    }

    fun getVars(): Map<String, String> {
        return vars
    }
}

interface SourceGroup {

    /**
     * @return 源文件列表
     */
    fun sourceFiles(paths: List<Path>): List<SourceFile>

    fun createDownloadTask(downloadPath: Path, options: DownloadOptions): DownloadTask
}

interface SourceFile {

    /**
     * @param downloadRootPath 下载路径，取决于下载器的设置
     * @return 文件在下载目录的路径
     */
    fun downloadSavePath(downloadRootPath: Path): Path

    /**
     * @return 模版变量
     */
    fun patternVars(): PatternVars

}