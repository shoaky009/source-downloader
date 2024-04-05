package io.github.shoaky.sourcedownloader.common.anitom

import com.dgtlrepublic.anitomyj.AnitomyJ
import com.google.common.base.CaseFormat
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.VariableProvider
import kotlin.io.path.name

/**
 * Anitom的变量提供
 */
class AnitomVariableProvider : VariableProvider {

    override fun itemVariables(sourceItem: SourceItem): PatternVariables = PatternVariables.EMPTY

    override fun fileVariables(
        sourceItem: SourceItem,
        itemVariables: PatternVariables,
        sourceFiles: List<SourceFile>
    ): List<PatternVariables> {
        return sourceFiles.map { file ->
            val parse = AnitomyJ.parse(file.path.name)
                .associateBy({
                    CaseFormat.UPPER_CAMEL.to(
                        CaseFormat.LOWER_CAMEL,
                        it.category.name.removePrefix("kElement")
                    )
                }, { it.value })
            MapPatternVariables(parse)
        }
    }

    override fun support(sourceItem: SourceItem): Boolean = true
}

