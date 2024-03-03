package io.github.shoaky.sourcedownloader.component.trigger

import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.concurrent.timerTask

/**
 * 固定时间间隔触发器
 */
class FixedScheduleTrigger(
    private val interval: Duration,
    private val onStartRunTasks: Boolean = false,
) : HoldingTaskTrigger() {

    private val timer = Timer("fixed-schedule:$interval")

    override fun stop() {
        timer.cancel()
        timer.purge()
    }

    override fun start() {
        timer.scheduleAtFixedRate(timerTask(), interval.toMillis(), interval.toMillis())
        if (onStartRunTasks) {
            getSourceGroupingTasks().forEach {
                Thread.ofVirtual().name("onstart-up-$interval")
                    .start {
                        it.run()
                    }
            }
        }
    }

    private fun timerTask() = timerTask {
        getSourceGroupingTasks().forEach { task ->
            Thread.ofVirtual().name("fixed-schedule-$interval")
                .start(task)
                .setUncaughtExceptionHandler { _, e ->
                    log.error("任务处理发生异常:{}", task, e)
                }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(FixedScheduleTrigger::class.java)
    }

}

