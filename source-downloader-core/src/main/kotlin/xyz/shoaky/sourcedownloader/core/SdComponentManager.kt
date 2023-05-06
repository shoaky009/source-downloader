package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.core.processor.SourceProcessor
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.SourceItemPointer
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.util.Events
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

interface SdComponentManager {

    fun createComponent(name: String, componentType: ComponentType, props: Properties)

    fun getAllProcessor(): List<SourceProcessor>

    fun getComponent(name: String): SdComponent?

    fun getAllComponent(): List<SdComponent>

    fun getSupplier(type: ComponentType): SdComponentSupplier<*>

    fun getSuppliers(): List<SdComponentSupplier<*>>

    fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>)

    fun getAllComponentNames(): Set<String>

    fun getAllTrigger(): List<Trigger> {
        return getAllComponent().filterIsInstance<Trigger>()
    }

    fun getAllSource(): List<Source<SourceItemPointer>> {
        return getAllComponent().filterIsInstance<Source<SourceItemPointer>>()
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

    fun getComponentDescriptions(): List<ComponentDescription>
    fun destroy(instanceName: String)
}

@Component
class SpringSdComponentManager(
    private val applicationContext: DefaultListableBeanFactory,
) : SdComponentManager {

    private val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun createComponent(name: String, componentType: ComponentType, props: Properties) {
        val beanName = componentType.instanceName(name)
        val exists = applicationContext.containsSingleton(beanName)
        if (exists) {
            throw ComponentException.instanceExists("component $beanName already exists, check your config.yaml and remove duplicate component")
        }

        val supplier = getSupplier(componentType)
        // FIXME 创建顺序问题
        val otherTypes = supplier.supplyTypes().filter { it != componentType }
        val singletonNames = applicationContext.singletonNames.toSet()
        for (otherType in otherTypes) {
            val typeBeanName = otherType.instanceName(name)
            if (singletonNames.contains(typeBeanName)) {
                val component = applicationContext.getBean(typeBeanName)
                applicationContext.registerSingleton(beanName, component)
                return
            }
        }

        val component = supplier.apply(props)
        if (applicationContext.containsBean(beanName).not()) {
            applicationContext.registerSingleton(beanName, component)
            Events.register(component)
        }
    }

    override fun getAllProcessor(): List<SourceProcessor> {
        return applicationContext.getBeansOfType(SourceProcessor::class.java).values.toList()
    }

    override fun getComponent(name: String): SdComponent? {
        return kotlin.runCatching {
            applicationContext.getBean(name, SdComponent::class.java)
        }.getOrNull()
    }

    override fun getAllComponent(): List<SdComponent> {
        return applicationContext.getBeansOfType(SdComponent::class.java).values.toList()
    }

    override fun getSupplier(type: ComponentType): SdComponentSupplier<*> {
        return sdComponentSuppliers[type]
            ?: throw ComponentException.unsupported("Supplier不存在, 组件类型:${type.topTypeClass.simpleName}:${type.typeName}")
    }

    override fun getSuppliers(): List<SdComponentSupplier<*>> {
        return sdComponentSuppliers.values.toList()
    }

    override fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>) {
        for (componentSupplier in sdComponentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.sdComponentSuppliers.containsKey(type)) {
                    throw ComponentException.supplierExists("组件类型已存在:${type}")
                }
                this.sdComponentSuppliers[type] = componentSupplier
            }
        }
    }

    override fun destroy(instanceName: String) {
        if (applicationContext.containsSingleton(instanceName)) {
            val bean = applicationContext.getBean(instanceName)
            if (bean is Trigger) {
                bean.stop()
            }
            applicationContext.destroySingleton(instanceName)
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = applicationContext.getBeansOfType(SdComponent::class.java)
        return type.keys
    }

    override fun getComponentDescriptions(): List<ComponentDescription> {
        return sdComponentSuppliers.values.distinct()
            .map { supplier ->
                val typeDesc = supplier.supplyTypes().groupBy { it.topType() }
                    .map { (topType, types) ->
                        TypeDescription(
                            topType,
                            types.map { it.typeName },
                            // TODO description
                            "",
                        )
                    }

                val componentClass = supplier::class.declaredMemberFunctions
                    .first {
                        it.name == "apply" && it.valueParameters.size == 1
                    }.returnType.jvmErasure

                val any = typeDesc.any { it.topType == Components.VARIABLE_PROVIDER }
                ComponentDescription(
                    componentClass.simpleName!!,
                    typeDesc,
                    emptyList(),
                    ruleDescriptions(supplier),
                    if (any) emptyList() else null
                )
            }
    }

    private fun ruleDescriptions(supplier: SdComponentSupplier<*>) =
        supplier.rules().map {
            RuleDescription(
                if (it.isAllow) "允许" else "禁止",
                it.type.lowerHyphenName(),
                it.value.simpleName!!
            )
        }
}