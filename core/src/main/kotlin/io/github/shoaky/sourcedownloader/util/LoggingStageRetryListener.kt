package io.github.shoaky.sourcedownloader.util

import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener

class LoggingStageRetryListener : RetryListener {

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>?,
        throwable: Throwable
    ) {
        val stage = context.getAttribute("stage")
        log.warn(
            "第{}次重试失败, {}, message:{}",
            context.retryCount,
            stage,
            "${throwable::class.simpleName}:${throwable.message}",
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger(LoggingStageRetryListener::class.java)

    }
}