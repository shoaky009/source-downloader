package xyz.shoaky.sourcedownloader.util.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import xyz.shoaky.sourcedownloader.core.CorePathPattern

class PathPatternDeserializer : StdDeserializer<CorePathPattern>(CorePathPattern::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CorePathPattern {
        val text = p?.text ?: throw IllegalArgumentException("text is null")
        return CorePathPattern(text)
    }
}