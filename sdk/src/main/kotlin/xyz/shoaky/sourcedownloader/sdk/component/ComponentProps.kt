package xyz.shoaky.sourcedownloader.sdk.component

import com.fasterxml.jackson.core.type.TypeReference
import xyz.shoaky.sourcedownloader.sdk.util.Jackson

class ComponentProps
private constructor(val properties: MutableMap<String, Any>) {

    inline fun <reified T> parse(): T {
        return Jackson.convert(properties, object : TypeReference<T>() {})
    }

    companion object {

        fun fromMap(map: Map<String, Any>): ComponentProps {
            return ComponentProps(HashMap(map))
        }

        fun fromJson(json: String): ComponentProps {
            val typeReference = object : TypeReference<MutableMap<String, Any>>() {}
            return ComponentProps(Jackson.fromJson(json, typeReference))
        }

    }
}

interface ComponentSupplier<R : SdComponent> {

    fun apply(props: ComponentProps): R
    fun availableTypes(): List<ComponentType>
}