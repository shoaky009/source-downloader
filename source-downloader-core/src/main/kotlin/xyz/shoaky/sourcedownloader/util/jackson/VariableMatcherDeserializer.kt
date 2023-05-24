package xyz.shoaky.sourcedownloader.util.jackson

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import xyz.shoaky.sourcedownloader.core.RegexVariableMatcher

class VariableMatcherDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctxt: DeserializationContext?): Any {
        return RegexVariableMatcher(Regex(key))
    }

}