package io.github.shoaky.sourcedownloader.application.minimize

import com.sun.net.httpserver.HttpServer
import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.SimpleObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.YamlConfigOperator
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultComponentManager
import io.github.shoaky.sourcedownloader.core.component.DefaultComponents
import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.core.processor.DefaultProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.repo.exposed.ExposedProcessingStorage
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.ProcessorService
import io.github.shoaky.sourcedownloader.util.StopWatch
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class SourceDownloaderMinimizeApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val stopWatch = StopWatch("SourceDownloaderApplication")
            stopWatch.start("application.config")
            val applicationConfig = ApplicationConfig(8080, SourceDownloaderConfig())
            stopWatch.stop()

            val context = createCoreApplication(args, applicationConfig, stopWatch)

            val server = HttpServer.create(
                InetSocketAddress(applicationConfig.port), 0
            )
            server.executor = Executors.newVirtualThreadPerTaskExecutor()

            server.createContext(
                "/api/application/reload",
                EnhancedHttpHandler {
                    context.coreApplication.reload()
                    it.sendResponseHeaders(200, 0)
                }
            )

            server.start()
        }

        private fun createCoreApplication(
            args: Array<String>,
            applicationConfig: ApplicationConfig,
            stopWatch: StopWatch,
        ): ApplicationContext {
            stopWatch.start("core.application.base")
            val dataLocation = applicationConfig.sourceDownloader.dataLocation
            val configPath = dataLocation.resolve("config.yaml")
            val configOperator = YamlConfigOperator(configPath)

            val props = SourceDownloaderProperties(dataLocation)
            val container = SimpleObjectWrapperContainer()
            val processingStorage: ProcessingStorage = ExposedProcessingStorage()
            val instanceManager = DefaultInstanceManager(configOperator)
            val componentManager: ComponentManager = DefaultComponentManager(
                container,
                listOf(
                    DefaultComponents(),
                    configOperator
                )
            )
            val pluginManager = PluginManager(
                componentManager,
                instanceManager,
                props
            )
            val processorManager: ProcessorManager =
                DefaultProcessorManager(processingStorage, componentManager, container)

            val application = CoreApplication(
                props,
                DefaultInstanceManager(configOperator),
                componentManager,
                processorManager,
                pluginManager,
                listOf(configOperator),
                listOf(
                )
            )

            configOperator.init()
            stopWatch.stop()

            stopWatch.start("core.application.start")
            application.start()
            stopWatch.stop()
            return ApplicationContext(
                instanceManager,
                componentManager,
                processorManager,
                pluginManager,
                configOperator,
                props,
                ComponentService(componentManager, configOperator),
                ProcessorService(processorManager, configOperator, processingStorage),
                ProcessingContentService(processingStorage, processorManager),
                application,
                processingStorage,
            )
        }
    }

}