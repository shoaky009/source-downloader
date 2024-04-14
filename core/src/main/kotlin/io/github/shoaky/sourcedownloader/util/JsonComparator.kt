package io.github.shoaky.sourcedownloader.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode

object JsonComparator {

    private val objectMapper = ObjectMapper()

    fun findDifference(json1: String, json2: String): JsonNode {
        val node1 = objectMapper.readTree(json1)
        val node2 = objectMapper.readTree(json2)
        return findDifference(node1, node2, "")
    }

    fun findDifference(node1: JsonNode, node2: JsonNode): JsonNode {
        return findDifference(node1, node2, "")
    }

    private fun findDifference(node1: JsonNode, node2: JsonNode, path: String): JsonNode {
        if (node1 == node2) return NullNode.instance // 如果节点相同，返回null

        if (node1.nodeType != node2.nodeType) {
            // 如果节点类型不同，直接返回第二个节点
            return node2
        }

        // 递归比较对象节点
        if (node1.isObject && node2.isObject) {
            val fieldNames1 = node1.fieldNames()
            val fieldNames2 = node2.fieldNames()

            val allFieldNames = HashSet<String>()
            fieldNames1.forEach { allFieldNames.add(it) }
            fieldNames2.forEach { allFieldNames.add(it) }

            val diffNode = objectMapper.createObjectNode()
            allFieldNames.forEach { fieldName ->
                val newPath = if (path.isNotEmpty()) "$path.$fieldName" else fieldName
                val childNode1 = node1.get(fieldName) ?: NullNode.instance
                val childNode2 = node2.get(fieldName) ?: NullNode.instance
                val diffChild = findDifference(childNode1, childNode2, newPath)
                if (diffChild != NullNode.instance) {
                    diffNode.set<JsonNode>(fieldName, diffChild)
                }
            }
            return if (diffNode.size() > 0) diffNode else NullNode.instance
        }

        // 递归比较数组节点
        if (node1.isArray && node2.isArray) {
            val size = maxOf(node1.size(), node2.size())
            val diffArray = objectMapper.createArrayNode()
            for (i in 0 until size) {
                val newPath = "$path[$i]"
                val childNode1 = if (i < node1.size()) node1.get(i) else NullNode.instance
                val childNode2 = if (i < node2.size()) node2.get(i) else NullNode.instance
                val diffChild = findDifference(childNode1, childNode2, newPath)
                if (diffChild != NullNode.instance) {
                    diffArray.add(diffChild)
                }
            }
            return if (diffArray.size() > 0) diffArray else NullNode.instance
        }

        // 直接比较值节点
        if (node1.isValueNode && node2.isValueNode && node1 != node2) {
            return node2
        }
        return NullNode.instance
    }
}