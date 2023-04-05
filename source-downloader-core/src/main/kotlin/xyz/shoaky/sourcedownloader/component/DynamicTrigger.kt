package xyz.shoaky.sourcedownloader.component

import com.google.common.eventbus.Subscribe
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.core.ProcessorSubmitDownloadEvent
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.concurrent.thread

class DynamicTrigger(config: Config) : HoldingTaskTrigger() {

    private val defaultInternal = config.defaultInternal.toMillis()
    private val bounds = config.boundProcessors

    private val hitTimeMap: MutableMap<DayOfWeek, MutableList<TriggerRange>> = mutableMapOf()

    private var thread: Thread? = null

    /**
     * 0:正常状态
     * 1:命中状态（触发器会每分钟触发一次）
     */
    @Volatile
    private var status = 0

    /**
     * status = 1时，hit方法被调用设置为true
     * 可以理解为这个区间的下载任务被执行过了,
     */
    @Volatile
    private var hit = false

    override fun start() {
        if (thread != null) {
            throw RuntimeException("DynamicTrigger has already started")
        }

        thread = thread {
            while (true) {
                val now = LocalDateTime.now()
                val localTime = now.toLocalTime()
                val nearest = findNearest(now.dayOfWeek, localTime)

                val nextInternal = nextInternal(localTime, nearest)
                val current = findCurrent(now.dayOfWeek, localTime)
                updateStatus(localTime, current)
                removeRange(now, nearest)

                if (log.isDebugEnabled) {
                    log.debug("DynamicTrigger nextInternal: {} status: {} nearest: {} map:{}", nextInternal, status,
                        nearest, hitTimeMap[now.dayOfWeek])
                }
                Thread.sleep(nextInternal)
                runTasks()
            }
        }
        thread?.start()
    }

    private fun removeRange(dateTime: LocalDateTime, nearest: TriggerRange?) {
        if (nearest == null) {
            return
        }
        if (status == 0 && hit) {
            return
        }

        val localTime = dateTime.toLocalTime()
        if (nearest.contains(localTime).not()) {
            hitTimeMap[dateTime.dayOfWeek]?.remove(nearest)
            log.debug("DynamicTrigger remove range: {}, week: {}", nearest, dateTime.dayOfWeek)
        }
        hit = false
    }

    private fun runTasks() {
        for (task in tasks) {
            kotlin.runCatching {
                task.run()
            }.onFailure {
                log.error("任务处理发生异常:{}", task, it)
            }
        }
    }

    private fun updateStatus(localTime: LocalTime, range: TriggerRange?) {
        if (range == null) {
            return
        }
        status = if (range.contains(localTime)) {
            1
        } else {
            0
        }
    }

    private fun nextInternal(localTime: LocalTime, nearest: TriggerRange?): Long {
        if (status == 1) {
            return Duration.ofMinutes(1L).toMillis()
        }
        if (nearest != null) {
            return Duration.between(localTime, nearest.start).toMillis()
        }
        return defaultInternal
    }

    override fun stop() {
        thread?.interrupt()
        thread = null
    }

    private fun findNearest(dayOfWeek: DayOfWeek, time: LocalTime): TriggerRange? {
        return hitTimeMap[dayOfWeek]?.filter { it.start > time }
            ?.minByOrNull { Duration.between(time, it.start).toMinutes() }
    }

    private fun findCurrent(dayOfWeek: DayOfWeek, time: LocalTime): TriggerRange? {
        return hitTimeMap[dayOfWeek]?.filter { it.contains(time) }
            ?.minByOrNull { Duration.between(time, it.start).toMinutes() }
    }

    @Subscribe
    @Suppress("unused")
    fun hit(event: ProcessorSubmitDownloadEvent) {
        val processorName = event.processorName
        if (bounds.contains(processorName).not()) {
            return
        }
        log.debug("DynamicTrigger hit: ${event.processorName}")

        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek
        synchronized(dayOfWeek) {
            addRange(dayOfWeek, now)
        }

        // 代表命中
        if (status == 1) {
            hit = true
        }

    }

    private fun addRange(dayOfWeek: DayOfWeek, now: LocalDateTime) {
        hitTimeMap[dayOfWeek]?.run {
            val range = TriggerRange(now)
            if (this.contains(range).not()) {
                this.add(range)
            }
        } ?: run {
            hitTimeMap[dayOfWeek] = mutableListOf(TriggerRange(now))
        }
    }

    class Config {
        val boundProcessors: Set<String> = emptySet()
        val defaultInternal = Duration.ofHours(2L)
    }

    class TriggerRange(hitTime: LocalDateTime) : ClosedRange<LocalTime> {

        override val endInclusive: LocalTime
        override val start: LocalTime

        init {
            val t1 = hitTime.plusMinutes(-10L)
            this.start = LocalTime.of(t1.hour, t1.minute, 0)

            val t2 = hitTime.plusMinutes(10L)
            this.endInclusive = LocalTime.of(t2.hour, t2.minute, 0)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TriggerRange

            if (endInclusive != other.endInclusive) return false
            return start == other.start
        }

        override fun hashCode(): Int {
            var result = endInclusive.hashCode()
            result = 31 * result + start.hashCode()
            return result
        }

    }
}

