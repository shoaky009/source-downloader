package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

data class ProcessorSourceState(
    var id: Long? = null,
    val processorName: String,
    val sourceId: String,
    val lastPointer: PersistentPointer,
    val retryTimes: Int = 0,
    val lastActiveTime: LocalDateTime = LocalDateTime.now()
) {

    companion object {

        fun <T : SourcePointer> resolvePointer(klass: KClass<out Source<T>>, values: Map<String, Any>): T {
            val first = klass.memberFunctions.first { m -> m.name == "fetch" && m.valueParameters.size == 2 }
                .valueParameters.map { it.type.jvmErasure }.filterIsInstance<KClass<T>>().first()
            return Jackson.convert(values, first)
        }
    }
}