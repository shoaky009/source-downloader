package io.github.shoaky.sourcedownloader.component.trigger

import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 固定时间间隔触发器
 */
class FixedScheduleTrigger(
    private val interval: Duration,
    private val onStartRunTasks: Boolean = false,
) : HoldingTaskTrigger() {

    private var f: ScheduledFuture<*>? = null

    override fun stop() {
        f?.cancel(false)
    }

    override fun start() {
        val intervalMilli = interval.toMillis()
        f = executor.scheduleAtFixedRate({
            getSourceGroupingTasks().forEach { task ->
                SourceProcessor.processExecutor.execute(task)
            }
        }, intervalMilli, intervalMilli, TimeUnit.MILLISECONDS)

        if (onStartRunTasks) {
            getSourceGroupingTasks().forEach {
                SourceProcessor.processExecutor.execute(it)
            }
        }
    }

    companion object {

        private val executor =
            Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("fixed-tick", 1).factory())
    }

}

