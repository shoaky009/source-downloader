package xyz.shoaky.sourcedownloader.api

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.shoaky.sourcedownloader.core.ComponentConfigStorage
import xyz.shoaky.sourcedownloader.core.SdComponentManager

@RestController
@RequestMapping("/api/component")
class ComponentController(
    private val componentManager: SdComponentManager,
    private val ccs: List<ComponentConfigStorage>
) {

    fun getComponents() {

    }

    fun createComponent() {

    }

    fun destroyComponent() {

    }

    fun getSupportComponentTypes() {

    }

}