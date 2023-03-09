package xyz.shoaky.sourcedownloader.util

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.HostAccess

object ScriptEngine {

    internal val jsEngine = GraalJSScriptEngine.create(
        Engine.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .build(),
        Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
    )
}