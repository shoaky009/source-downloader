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
) : TaskHolderTrigger() {

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

    override fun getComponentClass(): Class<FixedScheduleTrigger> {
        return FixedScheduleTrigger::class.java
    }

}