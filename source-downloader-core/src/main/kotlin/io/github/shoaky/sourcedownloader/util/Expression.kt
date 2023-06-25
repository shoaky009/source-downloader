package io.github.shoaky.sourcedownloader.util

import org.projectnessie.cel.EnvOption
import org.projectnessie.cel.Library
import org.projectnessie.cel.ProgramOption
import org.projectnessie.cel.checker.Decls
import org.projectnessie.cel.common.types.BoolT
import org.projectnessie.cel.interpreter.functions.Overload
import org.projectnessie.cel.tools.ScriptHost
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.notExists
import kotlin.io.path.readAttributes

internal val scriptHost = ScriptHost.newBuilder().build()

fun Path.creationTime(): LocalDateTime? {
    if (this.notExists()) {
        return null
    }
    val attrs = this.readAttributes<BasicFileAttributes>()
    val creationTime = attrs.creationTime()
    return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
}

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
            )
        )

        return listOf(option)
    }

    companion object {

        private const val CONTAINS_ANY = "containsAny"

        fun containsAny(source: List<String>, target: List<String>, ignoreCase: Boolean = false): Boolean {
            if (ignoreCase) {
                val set = target.map { it.lowercase() }.toSet()
                return source.any { it.lowercase() in set }
            }
            return source.any { it in target }
        }
    }
}