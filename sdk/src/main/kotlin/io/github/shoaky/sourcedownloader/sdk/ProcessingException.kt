package io.github.shoaky.sourcedownloader.sdk

import java.io.IOException

/**
 * @param skip if true processor will commit source state, witch means source will not be processed again,
 * use it when the [SourceItem] can not be processed any more, e.g. resource is 404
 */
class ProcessingException(
    message: String,
    val skip: Boolean = false
) : RuntimeException(message) {

    companion object {

        fun skippable(message: String): Throwable {
            return ProcessingException(message, true)
        }

        fun retryable(message: String, throwable: Throwable): Throwable {
            return IOException(message, throwable)
        }

        fun retryable(message: String): Throwable {
            return IOException(message)
        }
    }
}