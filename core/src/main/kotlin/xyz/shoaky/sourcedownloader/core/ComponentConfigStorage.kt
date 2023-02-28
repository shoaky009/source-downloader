package xyz.shoaky.sourcedownloader.core

import xyz.shoaky.sourcedownloader.core.config.ComponentConfig

interface ComponentConfigStorage {

    fun getAllComponents(): Map<String, List<ComponentConfig>>
}