package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

data class GetUserAll(
    // key is id
    val illusts: Map<Long, Any?> = emptyMap(),
    // 有数据时是Object,没数据时会返回[]
    val manga: JsonNode = NullNode.instance,
)