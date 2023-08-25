package io.github.shoaky.sourcedownloader.sdk.component

@Suppress("UNUSED")
class ComponentException
private constructor(message: String, val type: String) : RuntimeException(message) {

    companion object {
        @JvmStatic
        fun unsupported(message: String) = ComponentException(message, "unsupported")

        @JvmStatic
        fun missing(message: String) = ComponentException(message, "missing")

        @JvmStatic
        fun supplierExists(message: String) = ComponentException(message, "supplier:exists")

        @JvmStatic
        fun props(message: String) = ComponentException(message, "props:invalid")

        @JvmStatic
        fun compatibility(message: String) = ComponentException(message, "compatibility")

        @JvmStatic
        fun processorExists(message: String) = ComponentException(message, "processor:exists")

        @JvmStatic
        fun processorMissing(message: String) = ComponentException(message, "processor:missing")

        @JvmStatic
        fun instanceExists(message: String) = ComponentException(message, "instance:exists")

        @JvmStatic
        fun other(message: String) = ComponentException(message, "other")
    }
}