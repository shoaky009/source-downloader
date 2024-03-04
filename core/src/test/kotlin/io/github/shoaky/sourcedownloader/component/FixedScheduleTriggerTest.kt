package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.FixedScheduleTriggerSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.ProcessorTask
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.junit.jupiter.api.Test

class FixedScheduleTriggerTest {

    @Test
    fun should_run_after_started() {
        val trigger = FixedScheduleTriggerSupplier.apply(
            CoreContext.empty,
            Properties.fromMap(
                mapOf("interval" to "PT1M", "on-start-run-tasks" to true)
            )
        )

        var run = false
        var times = 0
        val task = Runnable {
            run = true
            times = times.inc()
        }
        trigger.addTask(ProcessorTask("test", task))
        trigger.start()
        Thread.sleep(10)
        assert(run)
    }

    @Test
    fun should_run() {
        val trigger = FixedScheduleTriggerSupplier.apply(
            CoreContext.empty,
            Properties.fromMap(
                mapOf("interval" to "PT1S")
            )
        )

        var run = false
        var times = 0
        val task = Runnable {
            run = true
            times = times.inc()
        }
        trigger.addTask(ProcessorTask("test", task))
        trigger.start()

        Thread.sleep(100L)
        assert(run.not())
        Thread.sleep(1000L)
        assert(run)
    }
}