package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.core.component.ObjectWrapper
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class SimpleObjectWrapperContainer : ObjectWrapperContainer {

    private val objects: MutableMap<String, ObjectWrapper<*>> = ConcurrentHashMap()

    override fun contains(name: String): Boolean {
        return objects.containsKey(name)
    }

    override fun put(name: String, value: ObjectWrapper<*>) {
        objects[name] = value
    }

    override fun <T : Any, W : ObjectWrapper<T>> get(name: String, typeRef: TypeReference<W>): W {
        @Suppress("UNCHECKED_CAST")
        return objects[name] as? W
            ?: throw IllegalArgumentException("Object $name cannot be cast to ${typeRef.type}")
    }

    override fun <T : Any, W : ObjectWrapper<T>> getObjectsOfType(typeRef: TypeReference<W>): Map<String, W> {
        val rawType = typeRef.type
        var actualTypeArguments: Array<out Type>? = null
        if (rawType is ParameterizedType) {
            actualTypeArguments = rawType.actualTypeArguments
        }

        @Suppress("UNCHECKED_CAST")
        return objects.filterValues { wrapper ->
            if (actualTypeArguments == null) {
                return@filterValues wrapper::class.java == rawType
            }
            if (actualTypeArguments.size > 1) {
                throw NotImplementedError("Multiple type arguments not implemented")
            }
            if (actualTypeArguments.size == 1 && wrapper::class.java == (typeRef.type as ParameterizedType).rawType) {
                val typeArgs = actualTypeArguments[0]
                if (typeArgs is ParameterizedType && typeArgs.rawType is Class<*>) {
                    val klass = typeArgs.rawType as Class<*>
                    return@filterValues klass.isAssignableFrom(wrapper.type())
                }
                if (typeArgs is Class<*>) {
                    return@filterValues typeArgs.isAssignableFrom(wrapper.type())
                }
                throw NotImplementedError("Multiple type arguments not implemented")
            }
            return@filterValues false
        } as Map<String, W>
    }

    override fun remove(name: String) {
        objects.remove(name)
    }

    override fun getAllObjectNames(): Set<String> {
        return objects.keys
    }

}