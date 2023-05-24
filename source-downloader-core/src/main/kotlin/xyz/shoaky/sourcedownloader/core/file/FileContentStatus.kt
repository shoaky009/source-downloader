package xyz.shoaky.sourcedownloader.core.file

enum class FileContentStatus {

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
    FILE_CONFLICT
}