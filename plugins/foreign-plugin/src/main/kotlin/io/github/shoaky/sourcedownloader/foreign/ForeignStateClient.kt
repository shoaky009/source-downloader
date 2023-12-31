package io.github.shoaky.sourcedownloader.foreign

import com.fasterxml.jackson.core.type.TypeReference

interface ForeignStateClient {

    fun <T : Any> postState(
        path: String,
        state: Any,
        typeReference: TypeReference<T>
    ): T

    fun <T : Any> getState(
        path: String,
        typeReference: TypeReference<T>
    ): T

}