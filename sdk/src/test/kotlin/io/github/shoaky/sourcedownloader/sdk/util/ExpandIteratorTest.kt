package io.github.shoaky.sourcedownloader.sdk.util

import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ExpandIteratorTest {

    @Test
    fun given_just_limited() {
        val result = ExpandIterator(
            listOf(1, 2, 3), 10
        ) {
            val start = (it - 1) * 10 + 1
            val end = it * 10
            val intRange = start..end
            IterationResult(intRange.toList(), true)
        }.toList()

        assertEquals(10, result.size)
    }

    @Test
    fun given_over_limited() {
        val result = ExpandIterator(
            listOf(1, 2, 3), 15
        ) {
            val start = (it - 1) * 10 + 1
            val end = it * 10
            val intRange = start..end
            IterationResult(intRange.toList(), true)
        }.toList()

        // 为什么是20而不是15，因为LimitedExpandIterator的实现是先获取到所有的数据，然后再进行limit的限制
        assertEquals(20, result.size)
    }

    @Test
    fun given_unlimited() {
        val result = ExpandIterator(
            listOf(1, 2, 3), Int.MAX_VALUE
        ) {
            val start = (it - 1) * 10 + 1
            val end = it * 10
            val intRange = start..end
            IterationResult(intRange.toList(), true)
        }.toList()
        assertContentEquals((1..30).toList(), result)
    }

    @Test
    fun test_terminated() {
        val result = ExpandIterator(
            listOf(AtomicInteger(1), AtomicInteger(3)), 30
        ) {
            val number = it.getAndIncrement()
            val start = (number - 1) * 10 + 1
            val end = number * 10
            val intRange = start..end
            IterationResult(intRange.toList(), number != 1)
        }.toList()
        assertContentEquals((1..30).toList(), result)
    }
}