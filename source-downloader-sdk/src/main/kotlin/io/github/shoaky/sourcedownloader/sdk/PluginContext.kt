package io.github.shoaky.sourcedownloader.sdk

import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import java.nio.file.Path

interface PluginContext {

    /**
     * In general if environment variable SOURCE_DOWNLOADER_HOME is set, then use it as the base directory.
     * otherwise use the current working directory as the base directory.
     *
     * @return the path to the directory where the application data is stored.
     */
    fun getPersistentDataPath(): Path

    fun registerSupplier(vararg suppliers: ComponentSupplier<*>)

    fun registerInstanceFactory(vararg factories: InstanceFactory<*>)


    /**
     * @param props 如果传入则不从instances的配置下读取
     *
     * 区别主要是针对用户配置的方式，因为[ComponentSupplier]的props是当前对应component下的值
     * 1.如果传props来创建对应组件的依赖对象，那么用户就不需要配置instances对应的属性
     * 2.如果不传props，那么就需要用户配置instances对应的属性
     * 两者各有优缺点，第一种方式用户配置简单，但是配置多的情况下，会导致配置文件很长
     * 第二种方式增加了用户配置的复杂性，但是配置文件相对简洁
     * 具体的可以看[TelegramSource]的实际情况，因为它单个实例只支持一个chatId，但是client实际是同一个
     * 用第一种配置会长，适合第二种配置方式
     * 相反[QbittorrentDownloader]就适合第一种配置方式
     */
    fun <T> loadInstance(name: String, klass: Class<T>, props: Properties? = null): T

    fun getInstanceManager(): InstanceManager
}