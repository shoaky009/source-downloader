package io.github.shoaky.sourcedownloader

import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessorConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.util.getObjectSuppliers
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import java.net.URI

@SpringBootApplication
// @ImportRuntimeHints(SourceDownloaderApplication.ApplicationRuntimeHints::class)
@EnableConfigurationProperties(SourceDownloaderProperties::class)
class SourceDownloaderApplication(
    private val environment: Environment,
    private val instanceManager: InstanceManager,
    private val componentManager: ComponentManager,
    private val processorManager: ProcessorManager,
    private val pluginManager: PluginManager,
    private val processorStorages: List<ProcessorConfigStorage>,
    private val componentStorages: List<ComponentConfigStorage>,
    private val componentSupplier: List<ComponentSupplier<*>>
) : InitializingBean {

    @EventListener(ApplicationReadyEvent::class)
    fun createProcessors() {
        log.info(
            "Database file located:${
                environment.getProperty("spring.datasource.url")
                    ?.removePrefix("jdbc:h2:file:")
            }"
        )

        val processorConfigs = processorStorages.flatMap { it.getAllProcessorConfig() }.filter { it.enabled }
        for (processorConfig in processorConfigs) {
            try {
                processorManager.createProcessor(processorConfig)
            } catch (e: Exception) {
                log.error("创建处理器失败:${processorConfig.name}")
                throw e
            }
        }
        componentManager.getAllTrigger()
            .forEach {
                it.start()
            }
    }

    @PreDestroy
    fun stopApplication() {
        pluginManager.destroyPlugins()
        componentManager.getAllTrigger()
            .forEach {
                it.stop()
            }
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
            *componentSupplier.toTypedArray()
        )
        componentManager.registerSupplier(
            *getObjectSuppliers("io.github.shoaky.sourcedownloader.component.supplier")
        )
        val types = componentManager.getSuppliers()
            .map { it.supplyTypes() }
            .flatten()
            .distinct()
            .groupBy({ it.topTypeClass.simpleName }, { it.typeName })
        log.info("组件注册完成:$types")
    }

    private fun createComponents() {
        componentManager.getSuppliers().filter {
            it.autoCreateDefault()
        }.forEach {
            for (type in it.supplyTypes()) {
                val typeName = type.typeName
                componentManager.createComponent(type, typeName, Properties.EMPTY)
                log.info("成功创建组件${type.topTypeClass.simpleName}:${typeName}:${typeName}")
            }
        }

        for (componentStorage in componentStorages) {
            componentStorage
                .getAllComponentConfig()
                .forEach(this::createFromConfigs)
        }
    }

    private fun createFromConfigs(key: String, configs: List<ComponentConfig>) {
        val componentKClass = ComponentTopType.fromName(key)?.klass
            ?: throw ComponentException.unsupported("未知组件类型:$key")

        configs
            .filter { it.enabled }
            .forEach {
                val type = ComponentType(it.type, componentKClass)
                try {
                    componentManager.createComponent(type, it.name, Properties.fromMap(it.props))
                } catch (e: Exception) {
                    log.error("创建组件失败:${type.topTypeClass.simpleName}:${it.type}:${it.name}")
                    throw e
                }
                log.info("成功创建组件${type.topTypeClass.simpleName}:${it.type}:${it.name}")
            }
    }

    companion object {

        internal val log = LoggerFactory.getLogger(SourceDownloaderApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            setupProxy()

            val springApplication = SpringApplication(SourceDownloaderApplication::class.java)
            springApplication.mainApplicationClass = SourceDownloaderApplication::class.java
            springApplication.run(*args)
        }

        private fun setupProxy() {
            val env = System.getenv()
            val urlStr = env["http_proxy"] ?: env["HTTP_PROXY"] ?: env["https_proxy"] ?: env["HTTPS_PROXY"]
            urlStr?.also {
                val url = URI(it)
                System.setProperty("http.proxyHost", url.host)
                System.setProperty("http.proxyPort", url.port.toString())
                System.setProperty("https.proxyHost", url.host)
                System.setProperty("https.proxyPort", url.port.toString())
            }
        }
    }

    override fun afterPropertiesSet() {
        // 加载出现异常不让应用完成启动
        loadAndInitPlugins()
        registerInstanceFactories()
        log.info("支持的组件类型:${ComponentType.types()}")
        registerComponentSuppliers()
        createComponents()
    }

    private fun registerInstanceFactories() {
        // instanceManager.registerInstanceFactory()
    }

    fun reload() {
        val processorNames = processorManager.getAllProcessorNames()
        for (name in processorNames) {
            processorManager.destroy(name)
        }

        val componentNames = componentManager.getAllComponentNames()
        for (name in componentNames) {
            componentManager.destroy(name)
        }

        val im = instanceManager
        if (im is DefaultInstanceManager) {
            im.destroyAll()
        }

        createComponents()
        createProcessors()
    }
}