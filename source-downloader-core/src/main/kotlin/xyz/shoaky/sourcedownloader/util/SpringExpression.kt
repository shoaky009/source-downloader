package xyz.shoaky.sourcedownloader.util

import org.projectnessie.cel.tools.ScriptHost
import org.springframework.util.unit.DataSize
import java.nio.file.Files
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

fun Path.lastModifiedTime(): LocalDateTime? {
    if (this.notExists()) {
        return null
    }
    val attrs = this.readAttributes<BasicFileAttributes>()
    val creationTime = attrs.lastModifiedTime()
    return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault())
}

fun Path.fileDataSize(): DataSize? {
    if (this.notExists()) {
        return null
    }
    val size = Files.size(this)
    return DataSize.ofBytes(size)
}