package xyz.shoaky.sourcedownloader.component

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.google.common.base.CaseFormat
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.name

class AnitomVariableProvider : VariableProvider {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return FunctionalItemGroup { path ->
            val parse = AnitomyJ.parse(path.name)
                .associateBy({
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                        it.category.name.removePrefix("kElement")
                    )
                }, { it.value })
            UniversalSourceFile(MapPatternVariables(parse))
        }
    }

    override fun support(item: SourceItem): Boolean = true
}

object AnitomVariableProviderSupplier : SdComponentSupplier<AnitomVariableProvider> {
    override fun apply(props: ComponentProps): AnitomVariableProvider {
        return AnitomVariableProvider()
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("anitom")
        )
    }

    override fun getComponentClass(): Class<AnitomVariableProvider> {
        return AnitomVariableProvider::class.java
    }
}