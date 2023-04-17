package xyz.shoaky.sourcedownloader.component.trigger

import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.concurrent.timerTask

class FixedScheduleTrigger(
    private val interval: Duration,
    private val onStartRunTasks: Boolean = false,
) : HoldingTaskTrigger() {

    private val timer = Timer("FixedScheduleTrigger")

    override fun stop() {
        timer.cancel()
        timer.purge()
    }

    override fun start() {
        timer.scheduleAtFixedRate(timerTask(), interval.toMillis(), interval.toMillis())
        if (onStartRunTasks) {
            tasks.forEach {
                it.run()
            }
        }
    }

    private fun timerTask() = timerTask {
        tasks.forEach { task ->
            kotlin.runCatching {
                task.run()
            }.onFailure {
                log.error("任务处理发生异常:{}", task, it)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FixedScheduleTrigger::class.java)
    }

}

