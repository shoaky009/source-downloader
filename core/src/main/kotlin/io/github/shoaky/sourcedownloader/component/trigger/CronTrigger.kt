package io.github.shoaky.sourcedownloader.component.trigger

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.springframework.util.Assert
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.*
import kotlin.concurrent.Volatile

class CronTrigger(
    expression: String
) : HoldingTaskTrigger() {

    private val taskScheduler = Scheduler()
    private val executionTime = Scheduler.spring53CronExecutionTime(expression)

    override fun start() {
        getSourceGroupingTasks().forEach {
            taskScheduler.schedule(executionTime, it)
        }
    }

    override fun stop() {
        taskScheduler.shutdown()
    }

    class Scheduler {

        private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

        fun schedule(executionTime: ExecutionTime, runnable: Runnable): ScheduledFuture<*>? {
            return ReschedulingRunnable(runnable, executionTime, Clock.systemDefaultZone(), executor).schedule()
        }

        fun schedule(expression: String, runnable: Runnable): ScheduledFuture<*>? {
            val executionTime = spring53CronExecutionTime(expression)
            return ReschedulingRunnable(runnable, executionTime, Clock.systemDefaultZone(), executor).schedule()
        }

        fun shutdown() {
            executor.shutdown()
        }

        companion object {

            fun spring53CronExecutionTime(expression: String): ExecutionTime {
                val def = CronDefinitionBuilder.instanceDefinitionFor(
                    CronType.SPRING53
                )
                val cron = CronParser(def).parse(expression)
                return ExecutionTime.forCron(cron)
            }
        }

    }

    private class ReschedulingRunnable(
        private val delegate: Runnable,
        private val executionTime: ExecutionTime,
        clock: Clock,
        private val executor: ScheduledExecutorService
    ) : ScheduledFuture<Any?>, Runnable {

        private val triggerContext = SimpleTriggerContext(clock)
        private var currentFuture: ScheduledFuture<*>? = null
        private var scheduledExecutionTime: Instant? = null

        private val triggerContextMonitor = Any()

        fun schedule(): ScheduledFuture<*>? {
            synchronized(this.triggerContextMonitor) {
                this.scheduledExecutionTime = executionTime.nextExecution(ZonedDateTime.now())
                    .map { it.toInstant() }.orElse(null)
                if (this.scheduledExecutionTime == null) {
                    return null
                }
                val delay = Duration.between(triggerContext.clock.instant(), this.scheduledExecutionTime)
                this.currentFuture = executor.schedule(this, delay.toNanos(), TimeUnit.NANOSECONDS)
                return this
            }
        }

        private fun obtainCurrentFuture(): ScheduledFuture<*>? {
            Assert.state(this.currentFuture != null, "No scheduled future")
            return this.currentFuture
        }

        override fun run() {
            val actualExecutionTime = triggerContext.clock.instant()
            delegate.run()
            val completionTime = triggerContext.clock.instant()
            synchronized(this.triggerContextMonitor) {
                Assert.state(this.scheduledExecutionTime != null, "No scheduled execution")
                triggerContext.update(this.scheduledExecutionTime, actualExecutionTime, completionTime)
                if (!obtainCurrentFuture()!!.isCancelled) {
                    schedule()
                }
            }
        }

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            synchronized(this.triggerContextMonitor) {
                return obtainCurrentFuture()!!.cancel(mayInterruptIfRunning)
            }
        }

        override fun isCancelled(): Boolean {
            synchronized(this.triggerContextMonitor) {
                return obtainCurrentFuture()!!.isCancelled
            }
        }

        override fun isDone(): Boolean {
            synchronized(this.triggerContextMonitor) {
                return obtainCurrentFuture()!!.isDone
            }
        }

        override fun get(): Any {
            var curr: ScheduledFuture<*>?
            synchronized(this.triggerContextMonitor) {
                curr = obtainCurrentFuture()
            }
            return curr!!.get()
        }

        override fun get(timeout: Long, unit: TimeUnit): Any {
            var curr: ScheduledFuture<*>?
            synchronized(this.triggerContextMonitor) {
                curr = obtainCurrentFuture()
            }
            return curr!![timeout, unit]
        }

        override fun getDelay(unit: TimeUnit): Long {
            var curr: ScheduledFuture<*>?
            synchronized(this.triggerContextMonitor) {
                curr = obtainCurrentFuture()
            }
            return curr!!.getDelay(unit)
        }

        override fun compareTo(other: Delayed): Int {
            if (this === other) {
                return 0
            }
            val diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
            return (if (diff == 0L) 0 else (if (diff < 0) -1 else 1))
        }
    }

    private class SimpleTriggerContext {

        val clock: Clock

        @Volatile
        var lastScheduledExecution: Instant? = null

        @Volatile
        var lastActualExecution: Instant? = null

        @Volatile
        var lastCompletion: Instant? = null

        constructor() {
            this.clock = Clock.systemDefaultZone()
        }

        constructor(clock: Clock) {
            this.clock = clock
        }

        fun update(
            lastScheduledExecution: Instant?,
            lastActualExecution: Instant?,
            lastCompletion: Instant?
        ) {
            this.lastScheduledExecution = lastScheduledExecution
            this.lastActualExecution = lastActualExecution
            this.lastCompletion = lastCompletion
        }

    }

}