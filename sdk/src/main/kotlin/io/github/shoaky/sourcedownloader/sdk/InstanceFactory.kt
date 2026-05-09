package io.github.shoaky.sourcedownloader.sdk

import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata

interface InstanceFactory<T> {

    fun create(props: Properties): T

    fun type(): Class<T>

    fun metadata(): ComponentMetadata? = null

}