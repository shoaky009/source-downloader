package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

// @Configuration
// @Component
class ComponentRegister(
    private val componentSupplier: List<SdComponentSupplier<*>>,
    private val componentStorages: List<ComponentConfigStorage>,
    private val reg: DefaultListableBeanFactory,
    private val c: ComponentManager
) : InitializingBean {

    fun registerComponent() {
        // val map = mutableMapOf<ComponentType, SdComponentSupplier<*>>()
        val map = c.sdComponentSuppliers

        for (componentStorage in componentStorages) {
            val allComponents = componentStorage.getAllComponents()
            for (entry in allComponents) {
                val componentKClass = ComponentType.typeOf(entry.key)
                if (componentKClass == null) {
                    SourceDownloaderApplication.log.warn("未知组件类型:${entry.key}")
                    return
                }

                entry.value.forEach {
                    val type = ComponentType(it.type, componentKClass)

                    val props = it.props
                    val sdComponentSupplier = map[type] ?: throw RuntimeException("not found $type")
                    val bd = BeanDefinitionBuilder.genericBeanDefinition(sdComponentSupplier.getComponentClass())
                    for (prop in props) {
                        bd.addPropertyValue(prop.key, prop.value)
                    }

                    reg.registerBeanDefinition(type.instanceName(it.name), bd.beanDefinition)

                    // componentManager.createComponent(type, it.name, it.props)
                    // SourceDownloaderApplication.log.info("成功创建组件${type.klass.simpleName}:${it.type}:${it.name}")
                }
            }
        }

        // for (supplier in componentSupplier) {
        //     for (type in supplier.supplyTypes()) {
        //         val beanName = type.name
        //         val beanDefinition = reg.getBeanDefinition(beanName)
        //         val beanClass = beanDefinition.beanClassName
        // val bean = reg.getBean(beanClass)
        // val beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass)
        // beanDefinitionBuilder.addPropertyValue("path", "test")
        // beanDefinitionBuilder.addPropertyValue("method", "GET")
        // beanDefinitionBuilder.addPropertyValue("requestMapping", bean)
        // reg.registerBeanDefinition(beanName, beanDefinitionBuilder.beanDefinition)
        // }
        // }
    }

    override fun afterPropertiesSet() {
        registerComponent()
    }
}