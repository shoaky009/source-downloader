package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.PersistentPointer
import java.time.LocalDateTime

/**
 * Processor状态
 */
data class ProcessorState(
    val sourceId:String,
    /**
     * Source当前处理的未知
     */
    val pointer: PersistentPointer,
    /**
     * 最后一次活跃时间
     */
    val lastActiveTime: LocalDateTime? = null,
    val retryTimes: Int = 0,
)