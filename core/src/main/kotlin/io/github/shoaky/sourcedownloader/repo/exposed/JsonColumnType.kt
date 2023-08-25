package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

inline fun <reified T : Any> Table.json(name: String) = json(name, { Jackson.toJsonString(it) }, { Jackson.fromJson(it, T::class) })
