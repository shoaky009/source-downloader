package xyz.shoaky.sourcedownloader.sdk.component

class ComponentException
private constructor(message: String, val type: String) : RuntimeException(message) {

    companion object {
        fun unsupported(message: String) = ComponentException(message, "unsupported")

        fun missing(message: String) = ComponentException(message, "missing")

        fun supplierExists(message: String) = ComponentException(message, "supplier:exists")
        fun props(message: String) = ComponentException(message, "props:invalid")

        fun compatibility(message: String) = ComponentException(message, "compatibility")

        fun processor(message: String) = ComponentException(message, "processor:exists")

        fun processorMissing(message: String) = ComponentException(message, "processor:missing")
    }
}