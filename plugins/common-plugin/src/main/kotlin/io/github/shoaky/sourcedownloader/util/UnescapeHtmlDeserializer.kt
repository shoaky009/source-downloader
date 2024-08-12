package io.github.shoaky.sourcedownloader.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import org.apache.commons.text.StringEscapeUtils

class UnescapeHtmlDeserializer : StringDeserializer() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
        val deserialize = super.deserialize(p, ctxt)
        return StringEscapeUtils.unescapeHtml4(deserialize)
    }
}