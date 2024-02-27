package io.github.shoaky.sourcedownloader.sdk.component

@Suppress("UNUSED")
class ComponentException(
    message: String,
    val type: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    companion object {

        @JvmStatic
        fun props(message: String) = ComponentException(message, "props:invalid")

        @JvmStatic
        fun props(message: String, cause: Throwable) = ComponentException(message, "props:invalid", cause)

        @JvmStatic
        fun compatibility(message: String) = ComponentException(message, "compatibility")

        @JvmStatic
        fun other(message: String) = ComponentException(message, "other")

        @JvmStatic
        fun processing(message: String) = ComponentException(message, "processing")
    }
}