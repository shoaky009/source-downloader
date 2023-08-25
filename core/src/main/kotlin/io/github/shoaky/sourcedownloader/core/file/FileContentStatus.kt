package io.github.shoaky.sourcedownloader.core.file

enum class FileContentStatus {

    UNDETECTED,

    /**
     * 正常没有任何文件冲突
     */
    NORMAL,

    /**
     * 路径模板变量不存在
     */
    VARIABLE_ERROR,

    /**
     * 目标文件已经存在
     */
    TARGET_EXISTS,

    /**
     * SourceItem中的目标文件冲突
     */
    FILE_CONFLICT,

    /**
     * 准备替换
     */
    READY_REPLACE,

    /**
     * 该文件是被替换了的
     */
    REPLACED,

    /**
     * 该文件是替换的
     */
    REPLACE

}