package io.github.shoaky.sourcedownloader.sdk.util

import com.google.common.reflect.ClassPath
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import java.net.URI
import java.util.*
import kotlin.reflect.KClass

fun String.find(vararg regexes: Regex): String? {
    for (regex in regexes) {
        val match = regex.find(this)
        if (match != null) {
            return match.value
        }
    }
    return null
}

fun String.replaces(replaces: List<String>, to: String, ignoreCase: Boolean = true): String {
    var result = this
    for (replace in replaces) {
        result = result.replace(replace, to, ignoreCase)
    }
    return result
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}

fun String.appendPrefix(append: Char): String {
    if (startsWith(append)) {
        return this
    }
    return "$append$this"
}

fun URI.queryMap(): Map<String, String> {
    return query.split("&").associate {
        val split = it.split("=")
        split[0] to split[1]
    }
}

const val KILOBYTE = 1024.0
const val MEGABYTE = KILOBYTE * 1024.0
const val GIGABYTE = MEGABYTE * 1024.0

val Long.readableRate: String
    get() = when {
        this > GIGABYTE -> {
            String.format("%.2f GiB/s", this / GIGABYTE)
        }

        this > MEGABYTE -> {
            String.format("%.2f MiB/s", this / MEGABYTE)
        }

        this > KILOBYTE -> {
            String.format("%.2f KiB/s", this / KILOBYTE)
        }

        else -> {
            "$this B/s"
        }
    }

val Long.readableSize: String
    get() = when {
        this > GIGABYTE -> {
            String.format("%.2f GiB", this / GIGABYTE)
        }

        this > MEGABYTE -> {
            String.format("%.2f MiB", this / MEGABYTE)
        }

        this > KILOBYTE -> {
            String.format("%.2f KiB", this / KILOBYTE)
        }

        else -> {
            "$this B"
        }
    }

// TODO native image会失败，后面再看
fun getObjectSuppliers(
    vararg packages: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader
): Array<ComponentSupplier<*>> {
    return packages.map {
        ClassPath.from(classLoader)
            .getTopLevelClasses(it)
    }
        .flatten()
        .filter { it.simpleName.contains("supplier", true) }
        .map { it.load().kotlin }
        .filterIsInstance<KClass<ComponentSupplier<*>>>()
        .mapNotNull {
            it.objectInstance
        }.toTypedArray()
}