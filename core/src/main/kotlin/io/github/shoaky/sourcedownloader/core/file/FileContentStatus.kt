package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.sdk.FileStatus
import java.util.*

enum class FileContentStatus : FileStatus {

    UNDETECTED,

    /**
     * 正常没有任何文件冲突
     */
    NORMAL,

    /**
     * 已下载
     */
    DOWNLOADED,

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
    REPLACE;

    fun isWarning(): Boolean {
        return this in (warningStatuses)
    }

    override fun status(): String {
        return name
    }

    override fun isSuccessful(): Boolean {
        return this in (successStatuses)
    }

    companion object {

        private val warningStatuses = EnumSet.of(VARIABLE_ERROR, TARGET_EXISTS, FILE_CONFLICT)
        private val successStatuses = EnumSet.of(NORMAL, REPLACED, REPLACE)
    }
}