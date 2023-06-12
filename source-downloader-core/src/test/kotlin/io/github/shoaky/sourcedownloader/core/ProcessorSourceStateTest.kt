package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import io.github.shoaky.sourcedownloader.sdk.component.Source
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class ProcessorSourceStateTest {

    @Test
    fun test_resolve_pointer() {
        val state = ProcessorSourceState(
            1,
            "test",
            "test",
            PersistentItemPointer(
                mutableMapOf(
                    "date" to "2022-01-01",
                    "id" to "1")
            ),
        )

        val pointer = state.resolvePointer(TestSource1::class)
        assertEquals(TestPointer1::class, pointer::class)
        assertEquals(LocalDate.of(2022, 1, 1), pointer.date)
        assertEquals("1", pointer.id)
    }
}

private object TestSource1 : Source<TestPointer1> {
    override fun fetch(pointer: TestPointer1?, limit: Int): Iterable<PointedItem<TestPointer1>> {
        return emptyList()
    }
}

data class TestPointer1(
    val date: LocalDate,
    val id: String
) : SourceItemPointer
