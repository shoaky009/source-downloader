package io.github.shoaky.sourcedownloader.core.processor

enum class DownloadStatus {
    FINISHED,
    NOT_FINISHED,
    NOT_FOUND;

    companion object {
        fun from(boolean: Boolean?): DownloadStatus {
            return when (boolean) {
                true -> FINISHED
                false -> NOT_FINISHED
                null -> NOT_FOUND
            }
        }
    }
}