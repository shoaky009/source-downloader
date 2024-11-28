package io.github.shoaky.sourcedownloader.application.spring

import io.github.shoaky.sourcedownloader.CoreApplication
import jakarta.annotation.PreDestroy
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.event.EventListener

@SpringBootApplication
@EnableConfigurationProperties(SpringSourceDownloaderProperties::class)
class SourceDownloaderSpringApplication(
    private val coreApplication: CoreApplication
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onReady() {
        coreApplication.start()
    }

    @PreDestroy
    fun stopApplication() {
        coreApplication.destroy()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val springApplication = SpringApplication(SourceDownloaderSpringApplication::class.java)
            springApplication.mainApplicationClass = SourceDownloaderSpringApplication::class.java
            springApplication.run(*args)
        }

    }
}