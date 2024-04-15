package io.github.shoaky.sourcedownloader.external.season

import io.github.shoaky.sourcedownloader.sdk.util.TextClear
import org.slf4j.LoggerFactory

class SeasonSupport(
    private val parsers: List<SeasonParser>,
    private val withDefault: Boolean = true
) {

    fun padValue(vararg subjects: ParseValue, length: Int = 2): String? {
        return input(*subjects)?.toString()?.padStart(length, '0')
    }

    fun input(vararg subjects: ParseValue): Int? {
        subjects
            .map {
                replaceValueIfNecessary(it)
            }
            .filter { it.value.isNotBlank() }
            .forEach {
                val res = if (it.anyResult) {
                    anyValue(it)
                } else {
                    voteValue(it)
                }
                if (res != null) {
                    return res
                }
            }
        if (withDefault) {
            return 1
        }
        return null
    }

    private fun replaceValueIfNecessary(subject: ParseValue): ParseValue {
        if (subject.preprocessValue) {
            val output = clear.input(subject.value).trim()
            if (output != subject.value) {
                return subject.copy(value = output)
            }
        }
        val trim = subject.value.trim()
        if (trim != subject.value) {
            return subject.copy(value = trim)
        }
        return subject
    }

    private fun voteValue(subject: ParseValue): Int? {
        val results = mutableMapOf<SeasonParser, SeasonResult?>()
        val chain = subject.chain(parsers)
        for (parse in chain) {
            parse.runCatching {
                results[parse] = parse.input(subject.value)
            }.onFailure {
                log.error("{} 发生异常, subject:$subject {}", parse::class.simpleName, subject, it)
            }
        }

        val value = results.values
            .filterNotNull()
            .groupingBy { it }
            .eachCount()
            .maxByOrNull {
                CmpValue(it.key, it.value)
            }?.key
        return value?.value
    }

    private fun anyValue(subject: ParseValue): Int? {
        val chain = subject.chain(parsers)
        for (rule in chain) {
            val res = rule.input(subject.value)
            if (res != null) {
                return res.value
            }
        }
        return null
    }

    companion object {

        private val log = LoggerFactory.getLogger(SeasonSupport::class.java)

        // 预留暂时没用
        private val clear = TextClear(
            mapOf()
        )

    }

    private data class CmpValue(val result: SeasonResult, val count: Int) : Comparable<CmpValue> {

        override fun compareTo(other: CmpValue): Int {
            val compareTo = result.compareTo(other.result)
            if (compareTo != 0) {
                return compareTo
            }
            return count.compareTo(other.count)
        }
    }

}

data class ParseValue(
    val value: String,
    val chainIndexes: List<Int> = emptyList(),
    val anyResult: Boolean = true,
    val preprocessValue: Boolean = true,
) {

    fun chain(parsers: List<SeasonParser>): List<SeasonParser> {
        if (chainIndexes.isEmpty()) {
            return parsers
        }
        return chainIndexes.map { parsers[it] }
    }
}
