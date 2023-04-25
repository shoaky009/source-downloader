package xyz.shoaky.sourcedownloader.common.anitom

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.google.common.base.CaseFormat
import xyz.shoaky.sourcedownloader.sdk.*
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

