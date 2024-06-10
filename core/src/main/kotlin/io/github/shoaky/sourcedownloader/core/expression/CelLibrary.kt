package io.github.shoaky.sourcedownloader.core.expression

import org.projectnessie.cel.EnvOption
import org.projectnessie.cel.Library
import org.projectnessie.cel.ProgramOption
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.common.types.BoolT
import org.projectnessie.cel.common.types.StringT
import org.projectnessie.cel.interpreter.functions.Overload

class CelLibrary : Library {

    override fun getCompileOptions(): List<EnvOption> {
        val options = EnvOption.declarations(
            Decls.newFunction(
                CONTAINS_ANY,
                Decls.newInstanceOverload(
                    "string_contains_any",
                    listOf(
                        Decls.newListType(Decls.String),
                        Decls.newListType(Decls.String)
                    ),
                    Decls.Bool
                ),
                Decls.newInstanceOverload(
                    "string_contains_any_ignore_case",
                    listOf(
                        Decls.newListType(Decls.String),
                        Decls.newListType(Decls.String),
                        Decls.Bool
                    ),
                    Decls.Bool
                )
            ),
            Decls.newFunction(
                JOIN_IGNORE_NULL,
                Decls.newInstanceOverload(
                    "string_join_ignore_null",
                    listOf(
                        Decls.newListType(Decls.String),
                        Decls.String,
                        Decls.String,
                    ),
                    Decls.String
                )
            )
        )
        return listOf(options)
    }

    override fun getProgramOptions(): List<ProgramOption> {
        val option = ProgramOption.functions(
            Overload.overload(
                CONTAINS_ANY,
                null,
                null,
                { l, r ->
                    val source = l.convertToNative(List::class.java).map { it.toString() }
                    val target = r.convertToNative(List::class.java).map { it.toString() }
                    val contains = containsAny(source, target)
                    if (contains) BoolT.True else BoolT.False
                },
                { vals ->
                    val l = vals[0].convertToNative(List::class.java).map { it.toString() }
                    val r = vals[1].convertToNative(List::class.java).map { it.toString() }
                    val b = vals[2].booleanValue()
                    val contains = containsAny(l, r, b)
                    if (contains) BoolT.True else BoolT.False
                },
            ),
            Overload.overload(
                JOIN_IGNORE_NULL,
                null,
                null,
                { d, c ->
                    val collection = d.convertToNative(List::class.java).map { it?.toString() }
                    val separator = c.convertToNative(String::class.java)
                    StringT.stringOf(joinIgnoreNull(collection, separator, ""))
                },
                { vals ->
                    val collection = vals[0].convertToNative(List::class.java).map { it?.toString() }
                    val separator = vals[1].convertToNative(String::class.java)
                    val prefix = vals[2].convertToNative(String::class.java)
                    StringT.stringOf(joinIgnoreNull(collection, separator, prefix))
                }
            )
        )

        return listOf(option)
    }

    /**
     * 设计接口要求表达式实现能够注册以下方法
     */
    companion object {

        private const val CONTAINS_ANY = "containsAny"
        private const val JOIN_IGNORE_NULL = "joinIgnoreNull"

        @JvmStatic
        fun containsAny(source: Collection<String>, target: Collection<String>, ignoreCase: Boolean = false): Boolean {
            if (ignoreCase) {
                val set = target.map { it.lowercase() }.toSet()
                return source.any { it.lowercase() in set }
            }
            return source.any { it in target }
        }

        @JvmStatic
        fun joinIgnoreNull(collection: Collection<String?>, separator: String, prefix: String): String {
            val filter = collection.filter { !it.isNullOrBlank() }
            if (filter.isEmpty()) return ""
            return filter.joinToString(separator, prefix)
        }
    }
}