package io.github.shoaky.sourcedownloader.sdk

interface Sleepable {

    fun inUse(): Boolean
    fun use(source: String)
    fun release(source: String)
}