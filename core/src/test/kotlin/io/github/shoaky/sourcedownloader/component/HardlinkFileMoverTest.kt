package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.HardlinkFileMoverSupplier
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.FixedItemContent
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import kotlin.io.path.deleteIfExists
import kotlin.test.assertEquals

class HardlinkFileMoverTest {

    @Test
    fun test() {
        val fileMover = HardlinkFileMoverSupplier.apply(CoreContext.empty, Properties.empty)
        val fileContent = createFileContent(
            testResourcePath.resolve("config.yaml").toAbsolutePath(),
            targetFilename = "config2.yaml"
        )
        val content = FixedItemContent(
            sourceItem(),
            listOf(
                fileContent
            )
        )
        val targetPath = fileContent.targetPath().toAbsolutePath()
        targetPath.deleteIfExists()
        val move = fileMover.move(content)
        assertEquals(true, move)
        targetPath.deleteIfExists()
    }
}