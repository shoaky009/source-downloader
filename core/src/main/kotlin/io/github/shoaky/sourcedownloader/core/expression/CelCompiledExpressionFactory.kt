package io.github.shoaky.sourcedownloader.core.expression

import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.tools.ScriptHost

object CelCompiledExpressionFactory : CompiledExpressionFactory {

    private val scriptHost = ScriptHost.newBuilder().build()

    override fun <T> create(raw: String, resultType: Class<T>, def: Map<String, VariableType>): CompiledExpression<T> {
        val defs = def.map { (variableName, variableType) ->
            when (variableType) {
                VariableType.STRING -> Decls.newVar(variableName, Decls.String)
                VariableType.BOOLEAN -> Decls.newVar(variableName, Decls.Bool)
                VariableType.MAP -> Decls.newVar(variableName, Decls.newMapType(Decls.String, Decls.Dyn))
                VariableType.ARRAY -> Decls.newVar(variableName, Decls.newListType(Decls.Dyn))
                VariableType.ANY -> Decls.newVar(variableName, Decls.Dyn)
                VariableType.DATE -> Decls.newVar(variableName, Decls.Timestamp)
            }
        }.toTypedArray()
        return scriptHost.buildScript(raw)
            .withDeclarations(*defs)
            .withLibraries(CelLibrary())
            .build()
            .let { CelCompiledExpression(it, resultType, raw) }
    }

}