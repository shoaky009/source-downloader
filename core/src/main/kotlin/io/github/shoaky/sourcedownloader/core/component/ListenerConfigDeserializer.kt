package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.contains
import io.github.shoaky.sourcedownloader.core.processor.ListenerMode

/**
 * 支持以下格式
 * - "test"
 * - "http:test"
 * - id: "test1"
 *   mode: BATCH
 * - id: "test2"
 * - "http:test3": BATCH
 */
class ListenerConfigDeserializer : StdDeserializer<ListenerConfig>(ListenerConfig::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ListenerConfig {
        val node = p.codec.readTree<JsonNode>(p)
        if (node is TextNode) {
            return ListenerConfig(ComponentId(node.asText()))
        }

        val hasId = node.contains("id")
        if (hasId) {
            val mode = node.get("mode")?.asText()?.let {
                ListenerMode.valueOf(it)
            } ?: ListenerMode.EACH
            return ListenerConfig(
                ComponentId(node.get("id").asText()),
                mode
            )
        }

        if (node !is ObjectNode) {
            throw IllegalArgumentException("不支持的格式 $node")
        }

        val firstNode = node.properties().first()
        return ListenerConfig(
            ComponentId(firstNode.key),
            ListenerMode.valueOf(firstNode.value.asText())
        )
    }
}