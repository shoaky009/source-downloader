package io.github.shoaky.sourcedownloader.core.expression.cel2

import com.google.common.collect.ImmutableSet
import dev.cel.checker.CelCheckerBuilder
import dev.cel.common.CelFunctionDecl
import dev.cel.common.CelOptions
import dev.cel.common.CelOverloadDecl
import dev.cel.common.types.ListType
import dev.cel.common.types.SimpleType
import dev.cel.common.values.NullValue
import dev.cel.compiler.CelCompilerLibrary
import dev.cel.extensions.CelExtensionLibrary
import dev.cel.runtime.CelFunctionBinding
import dev.cel.runtime.CelRuntimeBuilder
import dev.cel.runtime.CelRuntimeLibrary
import io.github.shoaky.sourcedownloader.core.expression.cel.CelLibrary
import java.util.*
import kotlin.jvm.optionals.getOrNull

object Cel2Lib : CelCompilerLibrary, CelRuntimeLibrary, CelExtensionLibrary.FeatureSet {

    private val functions: ImmutableSet<CelFunctionDecl> =
        ImmutableSet.of(
            CelFunctionDecl.newFunctionDeclaration(
                CelLibrary.CONTAINS_ANY,
                CelOverloadDecl.newMemberOverload(
                    "string_contains_any",
                    SimpleType.BOOL,
                    ListType.create(SimpleType.STRING), ListType.create(SimpleType.STRING)
                ),
                CelOverloadDecl.newMemberOverload(
                    "string_contains_any_decide_case",
                    SimpleType.BOOL,
                    ListType.create(SimpleType.STRING), ListType.create(SimpleType.STRING), SimpleType.BOOL
                )
            ),
            CelFunctionDecl.newFunctionDeclaration(
                CelLibrary.JOIN_IGNORE_NULL,
                CelOverloadDecl.newMemberOverload(
                    "strings_join_ignore_null",
                    SimpleType.STRING,
                    ListType.create(SimpleType.DYN), SimpleType.STRING, SimpleType.STRING
                )
            )
        )

    override fun setRuntimeOptions(runtimeBuilder: CelRuntimeBuilder) {
        runtimeBuilder.addFunctionBindings(
            CelFunctionBinding.from(
                "string_contains_any",
                listOf(List::class.java, List::class.java)
            ) { args ->
                val l = (args[0] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val r = (args[1] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                CelLibrary.containsAny(l, r, false)
            },
            CelFunctionBinding.from(
                "string_contains_any_decide_case",
                listOf(List::class.java, List::class.java, Boolean::class.javaObjectType)
            ) { args ->
                val l = (args[0] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val r = (args[1] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val b = args.getOrNull(2) as? Boolean ?: false
                CelLibrary.containsAny(l, r, b)
            },
            CelFunctionBinding.from(
                "strings_join_ignore_null",
                listOf(List::class.java, String::class.java, String::class.java)
            ) { args ->
                val strings = (args[0] as? List<*>)?.mapNotNull {
                    when (it) {
                        is Optional<*> -> it.getOrNull()?.toString()
                        is com.google.protobuf.NullValue -> null
                        is NullValue -> null
                        else -> it?.toString()
                    }
                } ?: emptyList()
                val separator = args[1]?.toString() ?: throw IllegalArgumentException("Separator cannot be null")
                CelLibrary.joinIgnoreNull(strings, separator, "")
            }
        )
    }

    override fun version(): Int {
        return 0
    }

    override fun functions(): ImmutableSet<CelFunctionDecl> {
        return functions
    }

    override fun setCheckerOptions(checkerBuilder: CelCheckerBuilder) {
        checkerBuilder.addFunctionDeclarations(functions)
            .setOptions(
                CelOptions.current()
                    .enableTimestampEpoch(true)
                    .build()
            )
    }


}