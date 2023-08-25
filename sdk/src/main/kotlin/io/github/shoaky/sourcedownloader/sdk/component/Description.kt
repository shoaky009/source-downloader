package io.github.shoaky.sourcedownloader.sdk.component

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class Property(
    val name: String,
    val type: KClass<out Any>,
    val description: String = "",
)

enum class PropertyType {
    STRING,
    INT,
    BOOLEAN,
    ARRAY,
    OBJECT,
    INSTANCE,
    AUTO
    ;
}
