package io.github.shoaky.sourcedownloader.component.trigger

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

class CronTrigger(
    expression: String
) : HoldingTaskTrigger() {

    private val trigger = CronTrigger(expression)
    private val taskScheduler = ThreadPoolTaskScheduler()

    override fun start() {
        taskScheduler.initialize()
        tasks.forEach {
            taskScheduler.schedule(
                it, trigger
            )
        }
    }

    override fun stop() {
        taskScheduler.shutdown()
    }
}