package xyz.shoaky.sourcedownloader.sdk

interface InstanceFactory<T> {
    fun create(props: Properties): T

}