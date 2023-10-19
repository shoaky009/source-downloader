package io.github.shoaky.sourcedownloader.sdk.util.masking

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class StringMaskingSerializer : StdSerializer<String>(
    String::class.java
), ContextualSerializer {

    private var stringMasking: StringMasking? = null

    override fun createContextual(prov: SerializerProvider, property: BeanProperty): JsonSerializer<*> {
        val masking = getAnnotation(property)
        if (masking != null) {
            val value = masking.value
            return INSTANCES.computeIfAbsent(value) { maskingType: KClass<out StringMasking> -> create(maskingType) }
        }
        return this
    }

    private fun create(maskingType: KClass<out StringMasking>): StringMaskingSerializer {
        if (stringMasking == null) {
            stringMasking = createMaskingInstance(maskingType)
        }
        return this
    }

    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        if (StringUtils.isBlank(value)) {
            gen.writeString(value)
            return
        }
        val mask = stringMasking!!.mask(value)
        gen.writeString(mask)
    }

    companion object {

        private val INSTANCES: MutableMap<KClass<out StringMasking>, StringMaskingSerializer> = ConcurrentHashMap()
        private fun createMaskingInstance(maskingType: KClass<out StringMasking>): StringMasking {
            return try {
                maskingType.primaryConstructor!!.call()
            } catch (e: InstantiationException) {
                throw UnsupportedOperationException(e)
            } catch (e: IllegalAccessException) {
                throw UnsupportedOperationException(e)
            }
        }

        private fun getAnnotation(property: BeanProperty?): Masking? {
            return property?.getAnnotation(Masking::class.java)
        }
    }
}
