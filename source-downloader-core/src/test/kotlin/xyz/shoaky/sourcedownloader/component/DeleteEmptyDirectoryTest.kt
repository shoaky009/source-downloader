package xyz.shoaky.sourcedownloader.component

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.component.supplier.CleanEmptyDirectorySupplier
import xyz.shoaky.sourcedownloader.core.CorePathPattern
import xyz.shoaky.sourcedownloader.core.file.CoreFileContent
import xyz.shoaky.sourcedownloader.core.file.PersistentSourceContent
import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sourceItem
import xyz.shoaky.sourcedownloader.testResourcePath
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
class DeleteEmptyDirectoryTest {

    private val cp = CleanEmptyDirectorySupplier.apply(Properties.EMPTY)

    @Test
    fun given_empty() {
        val itemDownloadPath = testResourcePath.resolve("ded")
        itemDownloadPath.deleteRecursively()

        itemDownloadPath.resolve("test").createDirectories()
        itemDownloadPath.resolve("daaaa").createDirectories()

        val sc = PersistentSourceContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    itemDownloadPath.resolve("test.mp4"),
                    Path(""),
                    testResourcePath,
                    MapPatternVariables(),
                    CorePathPattern.ORIGIN,
                    CorePathPattern.ORIGIN,
                )
            ),
            MapPatternVariables()
        )
        cp.accept(sc)
        assert(itemDownloadPath.notExists())
    }

    @Test
    fun given_not_empty() {
        val itemDownloadPath = testResourcePath.resolve("mockito-extensions")

        val sc = PersistentSourceContent(
            sourceItem(),
            listOf(
                CoreFileContent(
                    itemDownloadPath,
                    Path(""),
                    testResourcePath,
                    MapPatternVariables(),
                    CorePathPattern.ORIGIN,
                    CorePathPattern.ORIGIN,
                )
            ),
            MapPatternVariables()
        )
        cp.accept(sc)
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