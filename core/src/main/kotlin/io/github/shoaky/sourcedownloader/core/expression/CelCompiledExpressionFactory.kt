package io.github.shoaky.sourcedownloader.core.expression

import dev.cel.common.types.CelProtoTypes
import dev.cel.compiler.CelCompilerFactory
import dev.cel.compiler.CelCompilerLibrary
import dev.cel.extensions.CelExtensions
import dev.cel.parser.CelStandardMacro
import dev.cel.runtime.CelRuntime
import dev.cel.runtime.CelRuntimeFactory
import dev.cel.runtime.CelRuntimeLibrary
import io.github.shoaky.sourcedownloader.core.expression.cel2.Cel2CompiledExpression
import io.github.shoaky.sourcedownloader.core.expression.cel2.Cel2Lib

// object CelCompiledExpressionFactory : CompiledExpressionFactory {
//
//     private val scriptHost = ScriptHost.newBuilder()
//         .registry(JacksonRegistry.newRegistry())
//         .build()
//
//     override fun <T> create(raw: String, resultType: Class<T>, def: Map<String, VariableType>): CompiledExpression<T> {
//         val defs = def.map { (variableName, variableType) ->
//             when (variableType) {
//                 VariableType.STRING -> Decls.newVar(variableName, Decls.String)
//                 VariableType.BOOLEAN -> Decls.newVar(variableName, Decls.Bool)
//                 VariableType.INT -> Decls.newVar(variableName, Decls.Int)
//                 VariableType.MAP -> Decls.newVar(variableName, Decls.newMapType(Decls.String, Decls.Dyn))
//                 VariableType.ARRAY -> Decls.newVar(variableName, Decls.newListType(Decls.Dyn))
//                 VariableType.ANY -> Decls.newVar(variableName, Decls.Dyn)
//                 VariableType.DATE -> Decls.newVar(variableName, Decls.Timestamp)
//             }
//         }.toTypedArray()
//         return scriptHost.buildScript(raw)
//             .withDeclarations(*defs)
//             .withLibraries(CelLibrary(), StringsLib())
//             .build()
//             .let { CelCompiledExpression(it, resultType, raw) }
//     }
//
//     fun <T> create(raw: String, resultType: Class<T>): CompiledExpression<T> {
//         val sc = scriptHost.buildScript(raw)
//             .withDeclarations(
//                 Decls.newVar("item", Decls.newObjectType(SourceItem::class.java.name))
//             )
//             .withTypes(SourceItem::class.java)
//             .build()
//         return CelCompiledExpression(sc, resultType, raw)
//     }
//
// }

object CelCompiledExpressionFactory : CompiledExpressionFactory {

    private val compilerLibs: List<CelCompilerLibrary> =
        listOf(CelExtensions.strings(), CelExtensions.regex(), CelExtensions.optional(), Cel2Lib)
    private val runtimeLibs: List<CelRuntimeLibrary> =
        listOf(CelExtensions.strings(), CelExtensions.regex(), CelExtensions.optional(), Cel2Lib)

    private val celRuntime: CelRuntime = CelRuntimeFactory.standardCelRuntimeBuilder()
        .addLibraries(runtimeLibs)
        .build()

    override fun <T> create(raw: String, resultType: Class<T>, def: Map<String, VariableType>): CompiledExpression<T> {
        val builder = CelCompilerFactory.standardCelCompilerBuilder()
            .setStandardMacros(CelStandardMacro.STANDARD_MACROS)
            .addLibraries(compilerLibs)
        for ((variableName, variableType) in def) {
            when (variableType) {
                VariableType.STRING -> builder.addVar(variableName, CelProtoTypes.STRING)
                VariableType.BOOLEAN -> builder.addVar(variableName, CelProtoTypes.BOOL)
                VariableType.INT -> builder.addVar(variableName, CelProtoTypes.INT64)
                VariableType.MAP -> builder.addVar(
                    variableName,
                    CelProtoTypes.createMap(CelProtoTypes.STRING, CelProtoTypes.DYN)
                )

                VariableType.ARRAY -> builder.addVar(variableName, CelProtoTypes.createList(CelProtoTypes.DYN))
                VariableType.ANY -> builder.addVar(variableName, CelProtoTypes.DYN)
                VariableType.DATE -> builder.addVar(variableName, CelProtoTypes.TIMESTAMP)
            }
        }
        val compile = builder.build().compile(raw)
        val program = celRuntime.createProgram(compile.ast)
        return Cel2CompiledExpression(program, resultType, raw)
    }

}