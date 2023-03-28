package xyz.shoaky.sourcedownloader.sdk.component

import com.google.common.base.CaseFormat
import kotlin.reflect.KClass

data class ComponentType(val typeName: String,
    // TODO 限制密封类
                         val klass: KClass<out SdComponent>) {

    constructor(type: String, name: String) : this(name, componentTypes[type]
        ?: throw RuntimeException("不支持的类型$type, 支持的类型有${componentTypes.keys}"))

    companion object {
        fun downloader(type: String) = ComponentType(type, Downloader::class)
        fun source(type: String) = ComponentType(type, Source::class)
        fun fileMover(type: String) = ComponentType(type, FileMover::class)
        fun provider(type: String) = ComponentType(type, VariableProvider::class)
        fun itemFilter(type: String) = ComponentType(type, SourceItemFilter::class)
        fun trigger(type: String) = ComponentType(type, Trigger::class)
        fun run(type: String) = ComponentType(type, RunAfterCompletion::class)
        fun fileFilter(type: String) = ComponentType(type, SourceFileFilter::class)

        private val componentTypes = SdComponent::class.sealedSubclasses
            .associateBy {
                val simpleName = it.simpleName!!
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName)
            }

        fun typeOf(type: String): KClass<out SdComponent>? {
            return componentTypes[type]
        }

        fun types(): List<String> {
            return componentTypes.keys.toList()
        }
    }

    fun fullName() = "${klass.simpleName}:$typeName"

    fun instanceName(name: String) = "${fullName()}:${name}"

}