package io.github.shoaky.sourcedownloader.core.processor

import java.time.Instant

class ProcessorRuntime(
    val createdAt: Instant,
    var lastProcessFailedMessage: String? = null,
    var lastStartProcessTime: Instant? = null,
    var lastEndProcessTime: Instant? = null,
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
        val processing: Boolean
    ) {

        fun lastProcessDuration(): Long? {
            if (lastEndProcessTime == null || lastStartProcessTime == null) {
                return null
            }
            return lastEndProcessTime.toEpochMilli() - lastStartProcessTime.toEpochMilli()
        }
    }

}