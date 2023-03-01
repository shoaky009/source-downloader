package xyz.shoaky.sourcedownloader.core.component

import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Trigger
import java.time.Duration
import java.util.*
import kotlin.concurrent.timerTask

class FixedScheduleTrigger(
    private val interval: Duration,
    private val onStartRunTasks: Boolean = false,
) : Trigger {

    private val timer = Timer("FixedScheduleTrigger")
    private val tasks: MutableList<Runnable> = mutableListOf()

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

    override fun addTask(runnable: Runnable) {
        if (tasks.contains(runnable)) {
            return
        }
        tasks.add(runnable)
    }

    private fun timerTask() = timerTask {
        kotlin.runCatching {
            tasks.forEach {
                it.run()
            }
        }.onFailure {
            log.error("FixedScheduleTrigger处理发生异常", it)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FixedScheduleTrigger::class.java)
    }

}

object FixedScheduleTriggerSupplier : SdComponentSupplier<FixedScheduleTrigger> {
    override fun apply(props: ComponentProps): FixedScheduleTrigger {
        val interval = props.properties["interval"]?.toString()
            ?: throw IllegalArgumentException("interval is required")
        val onStartRunTasks = props.properties["on-start-run-tasks"]?.toString()
        return FixedScheduleTrigger(Duration.parse(interval), "true" == onStartRunTasks)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType("fixed", Trigger::class))
    }

}