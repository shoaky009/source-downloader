package io.github.shoaky.sourcedownloader.application.vertx

import io.github.shoaky.sourcedownloader.CoreApplication
import io.github.shoaky.sourcedownloader.config.SourceDownloaderProperties
import io.github.shoaky.sourcedownloader.core.PluginManager
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.service.ComponentService
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.ProcessorService
import io.vertx.ext.web.Router

class ApplicationContext(
    val instanceManager: InstanceManager,
    val componentManager: ComponentManager,
    val processorManager: ProcessorManager,
    val pluginManager: PluginManager,
    val configOperator: ConfigOperator,
    val props: SourceDownloaderProperties,
    val componentService: ComponentService,
    val processorService: ProcessorService,
    val processingContentService: ProcessingContentService,
    val coreApplication: CoreApplication,
    val processingStorage: ProcessingStorage,
    val webhookRouter: Router
)