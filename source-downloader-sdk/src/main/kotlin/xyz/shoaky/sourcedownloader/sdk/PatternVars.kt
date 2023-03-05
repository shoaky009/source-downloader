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

    fun copy(): PatternVars {
        return PatternVars(HashMap(vars))
    }
}

interface SourceGroup {

    /**
     * @return 源文件列表
     */
    fun sourceFiles(paths: List<Path>): List<SourceFile>

}

interface SourceFile {

    /**
     * @return 模版变量
     */
    fun patternVars(): PatternVars

}