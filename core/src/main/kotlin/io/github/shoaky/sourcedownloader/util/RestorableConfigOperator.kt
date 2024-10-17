package io.github.shoaky.sourcedownloader.util

import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

class RestorableConfigOperator(
    private val path: Path,
    private val configOperator: ConfigOperator
) : ConfigOperator by configOperator {

    private val cache = path.readText()

    fun restore() {
        path.writeText(cache)
    }
}