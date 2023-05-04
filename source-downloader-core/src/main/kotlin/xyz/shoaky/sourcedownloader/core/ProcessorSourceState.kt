package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.sdk.SourceItemPointer
import xyz.shoaky.sourcedownloader.sdk.component.Source
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

data class ProcessorSourceState(
    var id: Long? = null,
    val processorName: String,
    val sourceId: String,
    val lastPointer: PersistentItemPointer,
    val retryTimes: Int = 0,
    val lastActiveTime: LocalDateTime = LocalDateTime.now()
) {

    fun <T : SourceItemPointer> resolvePointer(klass: KClass<out Source<T>>): T {
        val first = klass.memberFunctions.first { m -> m.name == "fetch" && m.valueParameters.size == 2 }
            .valueParameters.map { it.type.jvmErasure }.filterIsInstance<KClass<T>>().first()
        return Jackson.convert(lastPointer, first)
    }
}