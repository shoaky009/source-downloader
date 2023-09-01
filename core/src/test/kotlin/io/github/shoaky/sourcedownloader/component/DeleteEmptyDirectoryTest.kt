package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.DeleteEmptyDirectorySupplier
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sourceItem
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
class DeleteEmptyDirectoryTest {

    private val cp = DeleteEmptyDirectorySupplier.apply(Properties.EMPTY)

    @Test
    fun given_empty() {
        val itemDownloadPath = testResourcePath.resolve("ded")
        itemDownloadPath.deleteRecursively()

        itemDownloadPath.resolve("test").createDirectories()
        itemDownloadPath.resolve("daaaa").createDirectories()
        val sc = CoreItemContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    itemDownloadPath.resolve("test.mp4"),
                    Path(""),
                    testResourcePath,
                    MapPatternVariables(),
                    CorePathPattern.ORIGIN,
                    CorePathPattern.ORIGIN,
                    Path(""),
                    ""
                )
            ),
            MapPatternVariables()
        )
        cp.onItemSuccess(sc)
        assert(itemDownloadPath.notExists())
    }

    @Test
    fun given_not_empty() {
        val itemDownloadPath = testResourcePath.resolve("mockito-extensions")
        val sc = CoreItemContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    itemDownloadPath,
                    Path(""),
                    testResourcePath,
                    MapPatternVariables(),
                    CorePathPattern.ORIGIN,
                    CorePathPattern.ORIGIN,
                    Path(""),
                    ""
                )
            ),
            MapPatternVariables()
        )
        cp.onItemSuccess(sc)
        assert(itemDownloadPath.exists())
    }

    companion object {

        private val itemDownloadPath = testResourcePath.resolve("ded")

        @JvmStatic
        @AfterAll
        fun clean() {
            itemDownloadPath.deleteRecursively()
        }
    }
}