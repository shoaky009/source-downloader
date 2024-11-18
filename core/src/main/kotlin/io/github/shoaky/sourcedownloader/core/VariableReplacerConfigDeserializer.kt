package io.github.shoaky.sourcedownloader.core

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.component.ComponentId
import io.github.shoaky.sourcedownloader.sdk.util.Jackson

/**
 * 支持以下格式
 * - "test"
 * - "http:test"
 * - id: "test1"
 *   keys: ["xxx"]
 * - id: "test2"
 */
class VariableReplacerConfigDeserializer : StdDeserializer<VariableReplacerConfig>(VariableReplacerConfig::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): VariableReplacerConfig {
        val node = p.codec.readTree<JsonNode>(p)
        if (node is TextNode) {
            return VariableReplacerConfig(ComponentId(node.asText()))
        }

        val hasId = node.contains("id")
        if (hasId.not()) {
            throw IllegalArgumentException("不支持的格式 $node")
        }
        val keysNode = node.get("keys")
        val keys = if (keysNode is ArrayNode) {
            Jackson.convert(keysNode, jacksonTypeRef<Set<String>>())
        } else {
            null
        }
        return VariableReplacerConfig(
            ComponentId(node.get("id").asText()),
            keys
        )
    }
}