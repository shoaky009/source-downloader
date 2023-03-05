package xyz.shoaky.sourcedownloader.core.component

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.HostAccess
import xyz.shoaky.sourcedownloader.sdk.*
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.SourceContentCreator
import java.nio.file.Path
import javax.script.SimpleBindings

class JavaScriptContentCreator(private val script: String) : SourceContentCreator {
    override fun createSourceGroup(sourceItem: SourceItem): SourceGroup {
        return ScriptFileGroup(sourceItem, script)
    }

    override fun defaultSavePathPattern(): PathPattern {
        return PathPattern.ORIGIN
    }

    override fun defaultFilenamePattern(): PathPattern {
        return PathPattern.ORIGIN
    }

}

private class ScriptFileGroup(
    val sourceItem: SourceItem,
    val script: String
) : SourceGroup {
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

    private val vars: PatternVars by lazy {
        eval()
    }

    override fun patternVars(): PatternVars {
        return vars
    }

    private fun eval(): PatternVars {
        val bindings = SimpleBindings()
        bindings["path"] = path.toString()
        bindings["item"] = sourceItem
        val vars = mutableMapOf<String, String>()
        bindings["vars"] = vars
        engine.eval(script, bindings)
        return PatternVars(vars)
    }

    companion object {
        private val engine = GraalJSScriptEngine.create(
            Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build(),
            Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowExperimentalOptions(true)
                .option("js.nashorn-compat", "true")
        )
    }
}

object ScriptContentCreatorSupplier : SdComponentSupplier<JavaScriptContentCreator> {
    override fun apply(props: ComponentProps): JavaScriptContentCreator {
        val script = props.properties["script"]?.toString()
            ?: throw RuntimeException("script is null")
        return JavaScriptContentCreator(script)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.creator("script"),
            ComponentType.creator("js"),
            ComponentType.creator("javascript"),
        )
    }

}