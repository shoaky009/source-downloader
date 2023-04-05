package xyz.shoaky.sourcedownloader

import com.google.common.reflect.ClassPath
import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import xyz.shoaky.sourcedownloader.component.*
import xyz.shoaky.sourcedownloader.component.supplier.*
import xyz.shoaky.sourcedownloader.core.*
import xyz.shoaky.sourcedownloader.core.config.ComponentConfig
import xyz.shoaky.sourcedownloader.core.config.ProcessorConfigs
import xyz.shoaky.sourcedownloader.external.qbittorrent.QbittorrentConfig
import xyz.shoaky.sourcedownloader.sdk.PathPattern
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import java.net.URL

@SpringBootApplication
@ImportRuntimeHints(SourceDownloaderApplication.ApplicationRuntimeHints::class)
class SourceDownloaderApplication(
    private val environment: Environment,
    private val componentManager: SdComponentManager,
    private val processorManager: ProcessorManager,
    private val pluginManager: PluginManager,
    private val processorStorages: List<ProcessorConfigStorage>,
    private val componentStorages: List<ComponentConfigStorage>,
    private val componentSupplier: List<SdComponentSupplier<*>>
) : InitializingBean {

    @EventListener(ApplicationReadyEvent::class)
    fun initApplication() {
        log.info("Database file located:${
            environment.getProperty("spring.datasource.url")
                ?.removePrefix("jdbc:h2:file:")
        }")

        val processorConfigs = processorStorages.flatMap { it.getAllProcessor() }
        for (processorConfig in processorConfigs) {
            processorManager.createProcessor(processorConfig)
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
            *getObjectSuppliers().toTypedArray()
        )
        componentManager.registerSupplier(
            *componentSupplier.toTypedArray()
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
        val componentKClass = ComponentType.typeOf(keyType)
        if (componentKClass == null) {
            log.warn("未知组件类型:$keyType")
            return
        }

        configs.forEach {
            val type = ComponentType(it.type, componentKClass)
            componentManager.createComponent(it.name, type, ComponentProps.fromMap(it.props))
            log.info("成功创建组件${type.klass.simpleName}:${it.type}:${it.name}")
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
                val url = URL(it)
                System.setProperty("http.proxyHost", url.host)
                System.setProperty("http.proxyPort", url.port.toString())
                System.setProperty("https.proxyHost", url.host)
                System.setProperty("https.proxyPort", url.port.toString())
            }
        }
    }

    class ApplicationRuntimeHints : RuntimeHintsRegistrar {
        override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
            hints.reflection().registerType(JsonType::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfigs::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig.Options::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(Regex::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(PathPattern::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig.ComponentId::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ProcessorConfig.Options::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(ComponentConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            hints.reflection().registerType(QbittorrentConfig::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)

            ClassPath.from(this::class.java.classLoader)
                .getTopLevelClasses("xyz.shoaky.sourcedownloader.component.supplier")
                .filter { it.simpleName.contains("supplier", true) }
                .map { it.load() }
                .forEach {
                    hints.reflection().registerType(it, MemberCategory.DECLARED_FIELDS)
                    hints.reflection().registerType(it, MemberCategory.INVOKE_PUBLIC_METHODS)
                }
        }

    }

    override fun afterPropertiesSet() {
        // 加载出现异常不让应用完成启动
        loadAndInitPlugins()
        log.info("支持的组件类型:${ComponentType.types()}")
        registerComponentSuppliers()
        createComponents()
        componentManager.getAllTrigger()
            .forEach {
                it.start()
            }
    }

    // private fun getObjectSuppliers(): List<SdComponentSupplier<*>> {
    //     return ClassPath.from(this::class.java.classLoader)
    //         .getTopLevelClasses("xyz.shoaky.sourcedownloader.component.supplier")
    //         .filter { it.simpleName.contains("supplier", true) }
    //         .map { it.load().kotlin }
    //         .filterIsInstance<KClass<SdComponentSupplier<*>>>()
    //         .mapNotNull {
    //             it.objectInstance
    //         }
    // }

    private fun getObjectSuppliers(): List<SdComponentSupplier<*>> {
        return listOf(
            QbittorrentSupplier,
            RssSourceSupplier,
            GeneralFileMoverSupplier,
            RunCommandSupplier,
            ExpressionItemFilterSupplier,
            FixedScheduleTriggerSupplier,
            UrlDownloaderSupplier,
            MockDownloaderSupplier,
            TouchItemDirectorySupplier,
            DynamicTriggerSupplier,
            SendHttpRequestSupplier,
            AiVariableProviderSupplier,
            SystemFileSourceSupplier,
            MetadataVariableProviderSupplier,
            JackettSourceSupplier,
            AnitomVariableProviderSupplier,
            SeasonProviderSupplier,
        )
    }
}