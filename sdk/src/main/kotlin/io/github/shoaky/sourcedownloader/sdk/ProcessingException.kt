package io.github.shoaky.sourcedownloader.sdk

/**
 * @param skip if true processor will commit source state, witch means source will not be processed again,
 * use it when the [SourceItem] can not be processed any more, e.g. resource is 404
 */
class ProcessingException(
    message: String,
    val skip: Boolean = false
) : RuntimeException(message) {

    companion object {

        fun skipThrow(message: String): Exception {
            throw ProcessingException(message, true)
        }
    }
}