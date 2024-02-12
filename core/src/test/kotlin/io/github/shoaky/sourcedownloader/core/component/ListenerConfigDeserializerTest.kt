package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.processor.ListenerMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListenerConfigDeserializerTest {

    @Test
    fun test() {
        val yamlMapper = YAMLMapper()
        val configs = yamlMapper.readValue(
            """
              - "test"
              - "http:test"
              - id: "test1"
                mode: "BATCH"
              - id: "test2"
              - "test3": EACH
              - "http:test4": BATCH
        """, jacksonTypeRef<List<ListenerConfig>>()
        )
        assertEquals(6, configs.size)
        assertEquals(ComponentId("http:test4"), configs.last().id)
        assertEquals(ListenerMode.BATCH, configs.last().mode)
    }
}