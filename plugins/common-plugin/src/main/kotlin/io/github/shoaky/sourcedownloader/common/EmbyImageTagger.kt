package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.component.FileTagger
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.notExists

object EmbyImageTagger : FileTagger {

    private val extensionToHandle = setOf("jpg", "jpeg", "png", "webp", "bmp")

    override fun tag(sourceFile: SourceFile): String? {
        val filename = sourceFile.path.name
        if (filename.contains("thumb", true)) {
            return "thumb"
        }
        if (filename.contains("poster", true)) {
            return "poster"
        }
        val extension = sourceFile.path.extension
        if (extensionToHandle.contains(extension).not()) {
            return null
        }
        if (sourceFile.path.notExists()) {
            return null
        }

        val image = ImageIO.read(sourceFile.path.toFile()) ?: return null
        if (image.width >= image.height) {
            return "thumb"
        }
        return "poster"
    }
}