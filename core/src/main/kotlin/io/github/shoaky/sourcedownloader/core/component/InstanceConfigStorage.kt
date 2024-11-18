package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.Properties

interface InstanceConfigStorage {

    fun getInstanceProps(name: String): Properties
}