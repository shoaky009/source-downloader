package io.github.shoaky.sourcedownloader

import io.github.shoaky.sourcedownloader.component.supplier.*
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessorConfigStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentFailureType
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.util.StopWatch
import org.slf4j.LoggerFactory
import java.net.URI

class CoreApplication(
    private val applicationProperties: SourceDownloaderProperties,
    private val instanceManager: InstanceManager,
    private val componentManager: ComponentManager,
    private val processorManager: ProcessorManager,
    private val pluginManager: PluginManager,
    private val processorStorages: List<ProcessorConfigStorage>,
    private val componentSupplier: List<ComponentSupplier<*>>,
) {

    private fun createProcessors() {
        val processorConfigs = processorStorages.flatMap { it.getAllProcessorConfig() }
        for (processorConfig in processorConfigs) {
            try {
                processorManager.createProcessor(processorConfig)
            } catch (e: Exception) {
                log.error("Failed to create processor ${processorConfig.name}")
                throw e
            }
        }
        componentManager.getAllTrigger()
            .forEach {
                log.info("Starting trigger ${it.componentName()}")
                it.component.start()
            }
    }

    fun destroy() {
        pluginManager.destroyPlugins()
        componentManager.getAllTrigger()
            .forEach {
                log.info("Stopping trigger ${it.componentName()}")
                it.component.stop()
            }
        destroyAllProcessor()
        destroyAllComponent()
        destroyAllInstance()
    }

    private fun loadAndInitPlugins() {
        pluginManager.loadPlugins()
        pluginManager.initPlugins()
    }

    private fun registerComponentSuppliers() {
        componentManager.registerSupplier(
            *componentSupplier.toTypedArray()
        )
        componentManager.registerSupplier(
            // *getObjectSuppliers(
            //     "io.github.shoaky.sourcedownloader.component.supplier",
            // )
            AlwaysReplaceSupplier,
            CompositeItemFileResolverSupplier,
            CompositeDownloaderSupplier,
            CronTriggerSupplier,
            DeleteEmptyDirectorySupplier,
            ExpressionFileFilterSupplier,
            ExpressionItemContentFilterSupplier,
            ExpressionItemFilterSupplier,
            FileSizeReplacementDeciderSupplier,
            FixedScheduleTriggerSupplier,
            FixedSourceSupplier,
            FullWidthReplacerSupplier,
            GeneralFileMoverSupplier,
            HardlinkFileMoverSupplier,
            HttpDownloaderSupplier,
            ItemDirectoryExistsDetectorSupplier,
            MappedFileTaggerSupplier,
            MockDownloaderSupplier,
            NeverReplaceSupplier,
            NoneDownloaderSupplier,
            RegexVariableProviderSupplier,
            RegexVariableReplacerSupplier,
            RunCommandSupplier,
            SendHttpRequestSupplier,
            SequenceVariableProviderSupplier,
            SystemFileResolverSupplier,
            SystemFileSourceSupplier,
            TouchItemDirectorySupplier,
            UriSourceSupplier,
            UrlDownloaderSupplier,
            UrlFileResolverSupplier,
            WindowsPathReplacerSupplier,
            ForceTrimmerSupplier,
            RegexTrimmerSupplier,
            KeywordIntegrationSupplier
        )
        val types = componentManager.getSuppliers()
            .map { it.supplyTypes() }
            .flatten()
            .distinct()
            .groupBy({ it.type.klass.simpleName }, { it.typeName })

        val builder = StringBuilder()
        builder.appendLine()
        for ((type, names) in types) {
            builder.appendLine("$type: $names")
        }
        log.info("Component supplier registration completed:{}", builder.toString())
    }

    fun start() {
        val stopWatch = StopWatch("CoreApplication")
        setupProxy()
        val dataPath = runCatching {
            applicationProperties.dataLocation.toAbsolutePath()
        }.getOrDefault(applicationProperties.dataLocation)
        log.info("Application data location:${dataPath}")

        stopWatch.start("core.application.plugins")
        loadAndInitPlugins()
        stopWatch.stop()

        stopWatch.start("core.application.instance-factories")
        registerInstanceFactories()
        stopWatch.stop()

        log.info("Supported component types:${ComponentType.types()}")
        stopWatch.start("core.application.component-suppliers")
        registerComponentSuppliers()
        stopWatch.stop()

        stopWatch.start("core.application.processors")
        createProcessors()
        stopWatch.stop()
        log.info("CoreApplication started in ${stopWatch.prettyPrint()}")
    }

    private fun registerInstanceFactories() {
        // Currently no instance factories are registered
    }

    fun reload() {
        destroyAllProcessor()
        destroyAllComponent()
        destroyAllInstance()
        createProcessors()
    }

    private fun destroyAllInstance() {
        if (instanceManager is DefaultInstanceManager) {
            instanceManager.destroyAll()
        }
        log.info("All instances destroyed")
    }

    private fun destroyAllComponent() {
        val components = componentManager.getAllComponent()
        for (component in components) {
            componentManager.destroy(component.type, component.name)
        }
        log.info("All components destroyed")
    }

    private fun destroyAllProcessor() {
        val processorNames = processorManager.getAllProcessorNames()
        for (name in processorNames) {
            processorManager.destroyProcessor(name)
        }
        log.info("All processors destroyed")
    }

    private fun setupProxy() {
        val env = System.getenv()
        val urlStr = env["http_proxy"]
            ?: env["HTTP_PROXY"]
            ?: env["https_proxy"]
            ?: env["HTTPS_PROXY"]
        urlStr?.also {
            val uri = URI(it)
            log.info("Proxy using:$uri")
            System.setProperty("http.proxyHost", uri.host)
            System.setProperty("http.proxyPort", uri.port.toString())
            System.setProperty("https.proxyHost", uri.host)
            System.setProperty("https.proxyPort", uri.port.toString())
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("CoreApplication")

        // 预留给springboot的gradle插件, 后面可以去掉
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }

}

fun throwComponentException(message: String, type: ComponentFailureType): Nothing {
    throw ComponentException(message, type.type)
}