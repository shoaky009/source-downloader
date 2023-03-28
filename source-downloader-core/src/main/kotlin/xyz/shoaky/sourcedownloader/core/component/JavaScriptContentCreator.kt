package xyz.shoaky.sourcedownloader.core.component

import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceContentCreator
import xyz.shoaky.sourcedownloader.util.ScriptEngine
import java.nio.file.Path
import javax.script.SimpleBindings

class JavaScriptContentCreator(private val script: String) : SourceContentCreator {
    override fun createSourceGroup(sourceItem: SourceItem): SourceItemGroup {
        return ScriptFileGroup(sourceItem, script)
    }

}

private class ScriptFileGroup(
    val sourceItem: SourceItem,
    val script: String
) : SourceItemGroup {
    override fun sourceFiles(paths: List<Path>): List<SourceFile> {
        return paths.map {
            ScriptFile(it, sourceItem, script)
        }
    }
}

class ScriptFile(
    private val path: Path,
    private val sourceItem: SourceItem,
    private val script: String
) : SourceFile {

    private val vars: PatternVariables by lazy {
        eval()
    }

    override fun patternVariables(): PatternVariables {
        return vars
    }

    private fun eval(): PatternVariables {
        val bindings = SimpleBindings()
        bindings["path"] = path.toString()
        bindings["item"] = sourceItem
        val vars = mutableMapOf<String, String>()
        bindings["vars"] = vars
        ScriptEngine.jsEngine.eval(script, bindings)
        return MapPatternVariables(vars)
    }
}

object ScriptContentCreatorSupplier : SdComponentSupplier<JavaScriptContentCreator> {
    override fun apply(props: ComponentProps): JavaScriptContentCreator {
        return JavaScriptContentCreator(props.get("script"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.creator("script"),
            ComponentType.creator("js"),
            ComponentType.creator("javascript"),
        )
    }

    override fun getComponentClass(): Class<JavaScriptContentCreator> {
        return JavaScriptContentCreator::class.java
    }

}