package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType

interface ComponentConfigStorage {

    /**
     * Key is [ComponentTopType], value is component config list
     */
    fun getAllComponentConfig(): Map<String, List<ComponentConfig>>

}