package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent

interface CoreContext {

    fun <T : SdComponent> getComponent(
        type: ComponentTopType,
        componentId: String,
        typeReference: TypeReference<T>,
    ): T

    companion object {

        val empty: CoreContext = EmptyContext

    }

    private object EmptyContext : CoreContext {

        override fun <T : SdComponent> getComponent(
            type: ComponentTopType,
            componentId: String,
            typeReference: TypeReference<T>,
        ): T {
            throw IllegalStateException("EmptyContext")
        }
    }

}

