package xyz.shoaky.sourcedownloader.contorller

import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.core.ComponentConfigStorage
import xyz.shoaky.sourcedownloader.core.ComponentManager
import xyz.shoaky.sourcedownloader.sdk.component.SdComponent

@RestController
@RequestMapping("/api/component")
class ComponentController(
    private val componentManager: ComponentManager,
    private val applicationContext: ApplicationContext,
    private val ccs: List<ComponentConfigStorage>
) {

    fun getComponents() {
        val components = applicationContext.getBeansOfType(SdComponent::class.java)
    }

    fun createComponent() {

    }

    fun destroyComponent() {

    }

    fun getSupportComponentTypes() {

    }

}