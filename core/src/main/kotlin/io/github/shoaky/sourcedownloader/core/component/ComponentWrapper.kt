package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.DelegateComponent
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent
import kotlin.reflect.KClass

data class ComponentWrapper<T : SdComponent>(
    val type: ComponentType,
    val name: String,
    val props: Properties,
    val component: T?,
    val primary: Boolean = true,
    val returnType: Class<*>,
    val errorMessage: String?
) : ObjectWrapper<T> {

    private val processorRef = mutableSetOf<String>()

    override fun get(): T {
        if (component != null) {
            return component
        }
        throw RuntimeException("Component ${type.fullName()} creation failed: $errorMessage")
    }

    override fun type(): Class<*> {
        return returnType
    }

    fun getAndMarkRef(ref: String, type: KClass<out SdComponent>): T {
        var res = get()
        if (res is DelegateComponent && !type.isInstance(res)) {
            @Suppress("UNCHECKED_CAST")
            res = res.getDelegate() as T
        }
        addRef(ref)
        return res
    }

    fun getOriginal(): SdComponent? {
        if (component is DelegateComponent) {
            return component.getDelegate()
        }
        return component
    }

    fun addRef(ref: String) {
        processorRef.add(ref)
    }

    fun removeRef(ref: String) {
        processorRef.remove(ref)
    }

    fun getRefs(): Set<String> {
        return processorRef
    }

    fun componentName(): String {
        return "${type.typeName}:$name"
    }

}

