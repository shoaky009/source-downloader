package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.util.StopWatch
import java.util.concurrent.atomic.AtomicInteger

class ProcessStat(
    private val name: String,
    private var processingCounting: AtomicInteger = AtomicInteger(0),
    private var filterCounting: AtomicInteger = AtomicInteger(0),
) {

    val stopWatch = StopWatch(name)

    fun incProcessingCounting() {
        processingCounting.incrementAndGet()
    }

    fun incFilterCounting() {
        filterCounting.incrementAndGet()
    }

    fun hasChange(): Boolean {
        return processingCounting.get() > 0 || filterCounting.get() > 0
    }

    override fun toString(): String {
        val sb = StringBuilder("'$name' 处理了${processingCounting}个 过滤了${filterCounting}个")
        for (task in stopWatch.taskInfo) {
            sb.append("; [").append(task.taskName).append("] took ").append(task.timeMillis).append(" ms")
            val percent = Math.round(100.0 * task.timeMillis / stopWatch.totalTimeMillis)
            sb.append(" = ").append(percent).append('%')
        }
        return sb.toString()
    }
}