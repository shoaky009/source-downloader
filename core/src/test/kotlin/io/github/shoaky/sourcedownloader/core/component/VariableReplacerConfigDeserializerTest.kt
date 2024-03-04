package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.VariableReplacerConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VariableReplacerConfigDeserializerTest {

    @Test
    fun test() {
        val yamlMapper = YAMLMapper()
        val configs = yamlMapper.readValue(
            """
              - "test"
              - "http:test"
              - id: "test1"
                keys: ["aaa", "bbb"]
              - id: "test2"
        """, jacksonTypeRef<List<VariableReplacerConfig>>()
        )
        assertEquals(4, configs.size)
    }
}