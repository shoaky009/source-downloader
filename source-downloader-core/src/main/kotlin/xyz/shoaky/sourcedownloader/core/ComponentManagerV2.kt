package xyz.shoaky.sourcedownloader.core

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.component.*

interface ComponentManagerV2 {

    fun getAllProcessor(): List<SourceProcessor>
    fun getProcessor(name: String): SourceProcessor?

    fun getComponent(name: String): SdComponent?

    fun getAllComponent(): List<SdComponent>

    fun getAllSource(): List<Source> {
        return getAllComponent().filterIsInstance<Source>()
    }

    fun getAllDownloader(): List<Downloader> {
        return getAllComponent().filterIsInstance<Downloader>()
    }

    fun getAllMover(): List<FileMover> {
        return getAllComponent().filterIsInstance<FileMover>()
    }

    fun getAllProvider(): List<VariableProvider> {
        return getAllComponent().filterIsInstance<VariableProvider>()
    }

}

@Component
class SpringComponentManager(
    private val applicationContext: ApplicationContext
) : ComponentManagerV2 {
    override fun getAllProcessor(): List<SourceProcessor> {
        return applicationContext.getBeansOfType(SourceProcessor::class.java).values.toList()
    }

    override fun getProcessor(name: String): SourceProcessor? {
        return applicationContext.getBeansOfType(SourceProcessor::class.java)
            .filter { it.value.name == name }.map { it.value }.firstOrNull()
    }

    override fun getComponent(name: String): SdComponent? {
        return kotlin.runCatching {
            applicationContext.getBean(name, SdComponent::class.java)
        }.getOrNull()
    }

    override fun getAllComponent(): List<SdComponent> {
        return applicationContext.getBeansOfType(SdComponent::class.java).values.toList()
    }

}