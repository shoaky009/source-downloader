package xyz.shoaky.sourcedownloader

import com.google.common.base.CaseFormat
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import xyz.shoaky.sourcedownloader.core.ComponentConfigStorage
import xyz.shoaky.sourcedownloader.core.ComponentManager
import xyz.shoaky.sourcedownloader.core.PluginManager
import xyz.shoaky.sourcedownloader.core.ProcessorConfigStorage
import xyz.shoaky.sourcedownloader.core.component.*
import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponent
import xyz.shoaky.sourcedownloader.sdk.component.Trigger

@SpringBootApplication
class SourceDownloaderApplication(
    private val environment: Environment,
    private val componentManager: ComponentManager,
    private val pluginManager: PluginManager,
    private val applicationContext: ApplicationContext,
    private val processorStorages: List<ProcessorConfigStorage>,
    private val componentStorages: List<ComponentConfigStorage>,
) {

    private val componentTypeMapping = SdComponent::class.sealedSubclasses
        .associateBy {
            val simpleName = it.simpleName
            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName!!)
        }

    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        log.info("Config file located:${environment.getProperty("spring.config.import")}")
        log.info("Sqlite file located:${
            environment.getProperty("spring.datasource.url")
                ?.removePrefix("jdbc:h2:file:")
        }")

        loadAndInitPlugins()

        log.info("支持的组件类型:${componentTypeMapping.keys}")
        registerComponentSuppliers()
        createComponents()
        val processorConfigs = processorStorages.flatMap { it.getAllProcessor() }
        for (processorConfig in processorConfigs) {
            componentManager.fullyCreateSourceProcessor(processorConfig)
        }
        applicationContext.getBeansOfType(Trigger::class.java).values
            .forEach {
                it.start()
            }
    }

    @PreDestroy
    fun onApplicationStopping() {
        pluginManager.destroyPlugins()
    }

    private fun loadAndInitPlugins() {
        pluginManager.loadPlugins()
        pluginManager.initPlugins()
        val plugins = pluginManager.getPlugins()
        plugins.forEach {
            val description = it.description()
            val fullName = description.fullName()
            log.info("成功加载插件$fullName")
        }
    }

    private fun registerComponentSuppliers() {
        componentManager.registerSupplier(
            *getDefaultComponentSuppliers().toTypedArray()
        )
        val types = componentManager.getSuppliers()
            .map { it.availableTypes() }
            .flatten()
            .distinct()
            .groupBy({ it.klass.simpleName }, { it.typeName })
        log.info("组件注册完成:$types")
    }

    private fun createComponents() {
        for (componentStorage in componentStorages) {
            componentStorage.getAllComponents()
                .forEach { (k, configs) ->
                    val componentKClass = componentTypeMapping[k]
                    if (componentKClass == null) {
                        log.warn("未知组件类型:$k")
                        return@forEach
                    }

                    configs.forEach {
                        val type = ComponentType(it.type, componentKClass)
                        componentManager.createComponent(type, it.name, it.props)
                        log.info("成功创建组件${type.klass.simpleName}:${it.type}:${it.name}")
                    }
                }
        }
    }

    companion object {
        internal val log = LoggerFactory.getLogger(SourceDownloaderApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SourceDownloaderApplication>(*args)
        }

        fun getDefaultComponentSuppliers(): List<ComponentSupplier<*>> {
            return listOf(
                QbittorrentSupplier,
                RssSourceSupplier,
                MoveFileSupplier,
                RunScriptSupplier,
                KeywordItemFilterSupplier,
                FixedScheduleTriggerSupplier,
                WatchFileSourceSupplier
            )
        }
    }

}
