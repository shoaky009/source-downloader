package io.github.shoaky.sourcedownloader.common.anime.parse

import org.slf4j.LoggerFactory

internal class ParserChain(
    private val parsers: List<ValueParser>,
    private val any: Boolean = false
) {
    fun apply(subject: SubjectContent, filename: String): Result {
        return if (any) {
            anyValue(subject, filename)
        } else {
            voteValue(subject, filename)
        }
    }

    private fun anyValue(subject: SubjectContent, filename: String): Result {
        for (parser in parsers) {
            val res = parser.apply(subject, filename)
            if (res.value != null) {
                return res
            }
        }
        return Result()
    }

    private fun voteValue(subject: SubjectContent, filename: String): Result {
        val results = mutableMapOf<String, Result>()
        for (parse in parsers) {
            parse.runCatching {
                results[parse.name] = parse.apply(subject, filename)
            }.onFailure {
                log.error("${parse.name} 发生异常,name:$subject filename:$filename {}", it)
            }
        }

        // 暂时先这样后面根据情况调整
        val value = results.values
            .filter { it.value != null }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull {
                CmpValue(it.key, it.value)
            }?.key
        return Result(value?.value)
    }

    private data class CmpValue(val result: Result, val count: Int) : Comparable<CmpValue> {
        override fun compareTo(other: CmpValue): Int {
            val compareTo = result.compareTo(other.result)
            if (compareTo != 0) {
                return compareTo
            }
            return count.compareTo(other.count)
        }
    }

    companion object {


        private val log = LoggerFactory.getLogger(ParserChain::class.java)
    }
}