package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

data class GetUserAll(
    // 有数据时是Object,没数据时会返回[]
    val illusts: JsonNode = NullNode.instance,
    // 有数据时是Object,没数据时会返回[]
    val manga: JsonNode = NullNode.instance,
)