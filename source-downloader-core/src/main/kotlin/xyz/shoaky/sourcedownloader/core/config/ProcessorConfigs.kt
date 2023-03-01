package xyz.shoaky.sourcedownloader.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import xyz.shoaky.sourcedownloader.core.ProcessorConfig

@ConfigurationProperties
data class ProcessorConfigs(val processors: List<ProcessorConfig> = emptyList())