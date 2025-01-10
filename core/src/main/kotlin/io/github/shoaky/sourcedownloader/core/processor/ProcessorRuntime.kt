package io.github.shoaky.sourcedownloader.core.processor

import java.time.Instant

class ProcessorRuntime(
    private val createdAt: Instant,
    var lastProcessFailedMessage: String? = null,
    private var lastStartProcessTime: Instant? = null,
    private var lastEndProcessTime: Instant? = null,
) {

    fun startProcessTime() {
        lastEndProcessTime = null
        lastStartProcessTime = Instant.now()
    }

    fun endProcessTime() {
        lastEndProcessTime = Instant.now()
    }

    data class Snapshot(
        val createdAt: Instant,
        val lastProcessFailedMessage: String?,
        val lastStartProcessTime: Instant?,
        val lastEndProcessTime: Instant?,
    ) {

        fun lastProcessDuration(): Long? {
            if (lastEndProcessTime == null || lastStartProcessTime == null) {
                return null
            }
            return lastEndProcessTime.toEpochMilli() - lastStartProcessTime.toEpochMilli()
        }
    }

    fun snapshot(): Snapshot {
        return Snapshot(createdAt, lastProcessFailedMessage, lastStartProcessTime, lastEndProcessTime)
    }
}