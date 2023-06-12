package io.github.shoaky.sourcedownloader.sdk.util

import com.google.common.reflect.ClassPath
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
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


fun URI.queryMap(): Map<String, String> {
    return query.split("&").associate {
        val split = it.split("=")
        split[0] to split[1]
    }
}


// TODO native image会失败，后面再看
fun getObjectSuppliers(
    vararg packages: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader
): Array<SdComponentSupplier<*>> {
    return packages.map {
        ClassPath.from(classLoader)
            .getTopLevelClasses(it)
    }
        .flatten()
        .filter { it.simpleName.contains("supplier", true) }
        .map { it.load().kotlin }
        .filterIsInstance<KClass<SdComponentSupplier<*>>>()
        .mapNotNull {
            it.objectInstance
        }.toTypedArray()
}