package xyz.shoaky.sourcedownloader

import com.google.common.base.CaseFormat
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import xyz.shoaky.sourcedownloader.api.qbittorrent.QbittorrentConfig
import xyz.shoaky.sourcedownloader.core.*
import xyz.shoaky.sourcedownloader.core.component.*
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfigs
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponent
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Trigger

@SpringBootApplication
@ImportRuntimeHints(SourceDownloaderApplication.ApplicationRuntimeHints::class)
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

    @EventListener(ApplicationReadyEvent::class)
    fun initApplication() {
        log.info("Config file located:${environment.getProperty("spring.config.import")}")
        log.info("Database file located:${
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
    fun stopApplication() {
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
            .map { it.supplyTypes() }
            .flatten()
            .distinct()
            .groupBy({ it.klass.simpleName }, { it.typeName })
        log.info("组件注册完成:$types")
    }

    private fun createComponents() {
        for (componentStorage in componentStorages) {
            componentStorage
                .getAllComponents()
                .forEach(this::createFromConfigs)
        }
    }

    private fun createFromConfigs(keyType: String, configs: List<ComponentConfig>) {
        val componentKClass = componentTypeMapping[keyType]
        if (componentKClass == null) {
            log.warn("未知组件类型:$keyType")
            return
        }

        configs.forEach {
            val type = ComponentType(it.type, componentKClass)
            componentManager.createComponent(type, it.name, it.props)
            log.info("成功创建组件${type.klass.simpleName}:${it.type}:${it.name}")
        }
    }

    companion object {
        internal val log = LoggerFactory.getLogger(SourceDownloaderApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val springApplication = SpringApplication(SourceDownloaderApplication::class.java)
            springApplication.mainApplicationClass = SourceDownloaderApplication::class.java
            springApplication.run(*args)
        }
    }

    class ApplicationRuntimeHints : RuntimeHintsRegistrar {
        override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
            hints.reflection().registerType(JsonType::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfigs::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(Regex::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(PathPattern::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig.ComponentId::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig.Options::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ComponentConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(QbittorrentConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
        }

    }

}

fun getDefaultComponentSuppliers(): List<SdComponentSupplier<*>> {
    return listOf(
        QbittorrentSupplier,
        RssSourceSupplier,
        MoveFileSupplier,
        RunScriptSupplier,
        RegexSourceItemFilterSupplier,
        FixedScheduleTriggerSupplier,
        WatchFileSourceSupplier,
        UrlDownloaderSupplier,
        MockDownloaderSupplier
    )
}