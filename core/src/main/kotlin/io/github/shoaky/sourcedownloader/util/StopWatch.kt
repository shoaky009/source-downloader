/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.shoaky.sourcedownloader.util

import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Simple stop watch, allowing for timing of a number of tasks, exposing total
 * running time and running time for each named task.
 *
 *
 * Conceals use of [System.nanoTime], improving the readability of
 * application code and reducing the likelihood of calculation errors.
 *
 *
 * Note that this object is not designed to be thread-safe and does not use
 * synchronization.
 *
 *
 * This class is normally used to verify performance during proof-of-concept
 * work and in development, rather than as part of production applications.
 *
 *
 * As of Spring Framework 5.2, running time is tracked and reported in
 * nanoseconds. As of 6.1, the default time unit for String renderings is
 * seconds with decimal points in nanosecond precision. Custom renderings with
 * specific time units can be requested through [.prettyPrint].
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since May 2, 2001
 * @see .start
 * @see .stop
 * @see .shortSummary
 * @see .prettyPrint
 */
class StopWatch
/**
 * Construct a new `StopWatch`.
 *
 * Does not start any task.
 */ @JvmOverloads constructor(
    /**
     * Identifier of this `StopWatch`.
     *
     * Handy when we have output from multiple stop watches and need to
     * distinguish between them in log or console output.
     */
    val id: String = ""
) {

    /**
     * Get the id of this `StopWatch`, as specified on construction.
     * @return the id (empty String by default)
     * @since 4.2.2
     * @see .StopWatch
     */

    private var taskList: MutableList<TaskInfo>? = ArrayList(1)

    /** Start time of the current task.  */
    private var startTimeNanos: Long = 0

    /** Name of the current task.  */
    private var currentTaskName: String? = null

    private var lastTaskInfo: TaskInfo? = null

    /**
     * Get the number of tasks timed.
     */
    var taskCount: Int = 0
        private set

    /**
     * Get the total time for all tasks in nanoseconds.
     * @since 5.2
     * @see .getTotalTime
     */
    /** Total running time.  */
    var totalTimeNanos: Long = 0
        private set

    /**
     * Construct a new `StopWatch` with the given id.
     *
     * The id is handy when we have output from multiple stop watches and need
     * to distinguish between them.
     *
     * Does not start any task.
     * @param id identifier for this stop watch
     */

    /**
     * Configure whether the [TaskInfo] array is built over time.
     *
     * Set this to `false` when using a `StopWatch` for millions of
     * tasks; otherwise, the `TaskInfo` structure will consume excessive memory.
     *
     * Default is `true`.
     */
    fun setKeepTaskList(keepTaskList: Boolean) {
        this.taskList = (if (keepTaskList) ArrayList() else null)
    }

    /**
     * Start a named task.
     *
     * The results are undefined if [.stop] or timing methods are
     * called without invoking this method first.
     * @param taskName the name of the task to start
     * @see .start
     * @see .stop
     */
    /**
     * Start an unnamed task.
     *
     * The results are undefined if [.stop] or timing methods are
     * called without invoking this method first.
     * @see .start
     * @see .stop
     */
    @JvmOverloads
    @Throws(IllegalStateException::class)
    fun start(taskName: String? = "") {
        check(this.currentTaskName == null) { "Can't start StopWatch: it's already running" }
        this.currentTaskName = taskName
        this.startTimeNanos = System.nanoTime()
    }

    /**
     * Stop the current task.
     *
     * The results are undefined if timing methods are called without invoking
     * at least one pair of `start()` / `stop()` methods.
     * @see .start
     * @see .start
     */
    @Throws(IllegalStateException::class)
    fun stop() {
        checkNotNull(this.currentTaskName) { "Can't stop StopWatch: it's not running" }
        val lastTime = System.nanoTime() - this.startTimeNanos
        this.totalTimeNanos += lastTime
        this.lastTaskInfo = TaskInfo(currentTaskName!!, lastTime)
        if (this.taskList != null) {
            taskList!!.add(lastTaskInfo!!)
        }
        ++this.taskCount
        this.currentTaskName = null
    }

    val isRunning: Boolean
        /**
         * Determine whether this `StopWatch` is currently running.
         * @see .currentTaskName
         */
        get() = (this.currentTaskName != null)

    /**
     * Get the name of the currently running task, if any.
     * @since 4.2.2
     * @see .isRunning
     */
    fun currentTaskName(): String? {
        return this.currentTaskName
    }

    /**
     * Get the last task as a [TaskInfo] object.
     * @throws IllegalStateException if no tasks have run yet
     * @since 6.1
     */
    @Throws(IllegalStateException::class)
    fun lastTaskInfo(): TaskInfo? {
        assert(this.lastTaskInfo != null) { "No tasks run" }
        return this.lastTaskInfo
    }

    /**
     * Get the last task as a [TaskInfo] object.
     */
    @Deprecated("as of 6.1, in favor of {@link #lastTaskInfo()}")
    @Throws(IllegalStateException::class)
    fun getLastTaskInfo(): TaskInfo? {
        return lastTaskInfo()
    }

    @get:Throws(IllegalStateException::class)
    @get:Deprecated("as of 6.1, in favor of {@link #lastTaskInfo()}")
    val lastTaskName: String
        /**
         * Get the name of the last task.
         * @see TaskInfo.getTaskName
         */
        get() = lastTaskInfo()!!.taskName

    @get:Throws(IllegalStateException::class)
    @get:Deprecated("as of 6.1, in favor of {@link #lastTaskInfo()}")
    val lastTaskTimeNanos: Long
        /**
         * Get the time taken by the last task in nanoseconds.
         * @since 5.2
         * @see TaskInfo.getTimeNanos
         */
        get() = lastTaskInfo()!!.timeNanos

    @get:Throws(IllegalStateException::class)
    @get:Deprecated("as of 6.1, in favor of {@link #lastTaskInfo()}")
    val lastTaskTimeMillis: Long
        /**
         * Get the time taken by the last task in milliseconds.
         * @see TaskInfo.getTimeMillis
         */
        get() = lastTaskInfo()!!.timeMillis

    val taskInfo: Array<TaskInfo>
        /**
         * Get an array of the data for tasks performed.
         * @see .setKeepTaskList
         */
        get() {
            if (this.taskList == null) {
                throw UnsupportedOperationException("Task info is not being kept!")
            }
            return taskList!!.toTypedArray<TaskInfo>()
        }

    val totalTimeMillis: Long
        /**
         * Get the total time for all tasks in milliseconds.
         * @see .getTotalTime
         */
        get() = TimeUnit.NANOSECONDS.toMillis(this.totalTimeNanos)

    val totalTimeSeconds: Double
        /**
         * Get the total time for all tasks in seconds.
         * @see .getTotalTime
         */
        get() = getTotalTime(TimeUnit.SECONDS)

    /**
     * Get the total time for all tasks in the requested time unit
     * (with decimal points in nanosecond precision).
     * @param timeUnit the unit to use
     * @since 6.1
     * @see .getTotalTimeNanos
     * @see .getTotalTimeMillis
     * @see .getTotalTimeSeconds
     */
    fun getTotalTime(timeUnit: TimeUnit?): Double {
        return totalTimeNanos.toDouble() / TimeUnit.NANOSECONDS.convert(1, timeUnit)
    }

    /**
     * Generate a table describing all tasks performed in the requested time unit
     * (with decimal points in nanosecond precision).
     *
     * For custom reporting, call [.getTaskInfo] and use the data directly.
     * @param timeUnit the unit to use for rendering total time and task time
     * @since 6.1
     * @see .prettyPrint
     * @see .getTotalTime
     * @see TaskInfo.getTime
     */
    /**
     * Generate a table describing all tasks performed in seconds
     * (with decimal points in nanosecond precision).
     *
     * For custom reporting, call [.getTaskInfo] and use the data directly.
     * @see .prettyPrint
     * @see .getTotalTimeSeconds
     * @see TaskInfo.getTimeSeconds
     */
    @JvmOverloads
    fun prettyPrint(timeUnit: TimeUnit = TimeUnit.SECONDS): String {
        val nf = NumberFormat.getNumberInstance(Locale.ENGLISH)
        nf.maximumFractionDigits = 9
        nf.isGroupingUsed = false

        val pf = NumberFormat.getPercentInstance(Locale.ENGLISH)
        pf.minimumIntegerDigits = 2
        pf.isGroupingUsed = false

        val sb = StringBuilder(128)
        sb.append("StopWatch '").append(id).append("': ")
        val total =
            (if (timeUnit == TimeUnit.NANOSECONDS) nf.format(totalTimeNanos) else nf.format(getTotalTime(timeUnit)))
        sb.append(total).append(" ").append(timeUnit.name.lowercase())
        val width = max(sb.length.toDouble(), 40.0).toInt()
        sb.append("\n")

        if (this.taskList != null) {
            val line = "-".repeat(width) + "\n"
            var unitName = timeUnit.name
            unitName = unitName[0].toString() + unitName.substring(1).lowercase()
            unitName = String.format("%-12s", unitName)
            sb.append(line)
            sb.append(unitName).append("  %       Task name\n")
            sb.append(line)

            var digits = total.indexOf('.')
            if (digits < 0) {
                digits = total.length
            }
            nf.minimumIntegerDigits = digits
            nf.maximumFractionDigits = 10 - digits

            for (task in taskList!!) {
                sb.append(
                    String.format(
                        "%-14s", (if (timeUnit == TimeUnit.NANOSECONDS) nf.format(
                            task.timeNanos
                        ) else nf.format(task.getTime(timeUnit)))
                    )
                )
                sb.append(
                    String.format(
                        "%-8s",
                        pf.format(task.timeSeconds / totalTimeSeconds)
                    )
                )
                sb.append(task.taskName).append('\n')
            }
        } else {
            sb.append("No task info kept")
        }

        return sb.toString()
    }

    /**
     * Get a short description of the total running time in seconds.
     * @see .prettyPrint
     * @see .prettyPrint
     */
    fun shortSummary(): String {
        return "StopWatch '" + id + "': " + totalTimeSeconds + " seconds"
    }

    /**
     * Generate an informative string describing all tasks performed in seconds.
     * @see .prettyPrint
     * @see .prettyPrint
     */
    override fun toString(): String {
        val sb = StringBuilder(shortSummary())
        if (this.taskList != null) {
            for (task in taskList!!) {
                sb.append("; [").append(task.taskName).append("] took ").append(task.timeSeconds).append(" seconds")
                val percent = Math.round(100.0 * task.timeSeconds / totalTimeSeconds)
                sb.append(" = ").append(percent).append('%')
            }
        } else {
            sb.append("; no task info kept")
        }
        return sb.toString()
    }

    /**
     * Nested class to hold data about one task executed within the `StopWatch`.
     */
    class TaskInfo internal constructor(
        /**
         * Get the name of this task.
         */
        val taskName: String,
        /**
         * Get the time this task took in nanoseconds.
         * @since 5.2
         * @see .getTime
         */
        val timeNanos: Long
    ) {

        val timeMillis: Long
            /**
             * Get the time this task took in milliseconds.
             * @see .getTime
             */
            get() = TimeUnit.NANOSECONDS.toMillis(this.timeNanos)

        val timeSeconds: Double
            /**
             * Get the time this task took in seconds.
             * @see .getTime
             */
            get() = getTime(TimeUnit.SECONDS)

        /**
         * Get the time this task took in the requested time unit
         * (with decimal points in nanosecond precision).
         * @param timeUnit the unit to use
         * @since 6.1
         * @see .getTimeNanos
         * @see .getTimeMillis
         * @see .getTimeSeconds
         */
        fun getTime(timeUnit: TimeUnit?): Double {
            return timeNanos.toDouble() / TimeUnit.NANOSECONDS.convert(1, timeUnit)
        }
    }
}