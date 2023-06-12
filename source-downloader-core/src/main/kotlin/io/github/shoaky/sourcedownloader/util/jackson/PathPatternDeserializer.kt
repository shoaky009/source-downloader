package io.github.shoaky.sourcedownloader.util.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.shoaky.sourcedownloader.core.CorePathPattern

class PathPatternDeserializer : StdDeserializer<CorePathPattern>(CorePathPattern::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CorePathPattern {
        val text = p?.text ?: throw IllegalArgumentException("text is null")
        return CorePathPattern(text)
    }
}

val yamlMapper: YAMLMapper = run {
    val mapper = YAMLMapper()
    mapper
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
    mapper
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
        .enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    mapper
}