package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
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
            PersistentPointer(
                mutableMapOf(
                    "date" to "2022-01-01",
                    "id" to "1")
            ),
        )

        val pointer = ProcessorSourceState.resolvePointer(TestSource1::class, state.lastPointer.values)
        assertEquals(TestPointer1::class, pointer::class)
        assertEquals(LocalDate.of(2022, 1, 1), pointer.date)
        assertEquals("1", pointer.id)
    }
}

private object TestSource1 : Source<TestPointer1> {

    override fun fetch(pointer: TestPointer1, limit: Int): Iterable<PointedItem<ItemPointer>> {
        return emptyList()
    }

    override fun defaultPointer(): TestPointer1 {
        throw NotImplementedError()
    }
}

data class TestPointer1(
    val date: LocalDate,
    val id: String
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        throw NotImplementedError()
    }
}
