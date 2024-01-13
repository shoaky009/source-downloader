package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.component.supplier.ExpressionFileFilterSupplier
import io.github.shoaky.sourcedownloader.sdk.FixedFileContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.testResourcePath
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.fileSize
import kotlin.test.assertEquals

class ExpressionFileFilterTest {
    @Test
    fun test_simple_exclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            listOf("file.name == '1.txt'")
        )
        val testFileContent1 = FixedFileContent(Path("1.txt"))
        assertEquals(false, filter.test(testFileContent1))
        val testFileContent2 = FixedFileContent(Path("2.txt"))
        assertEquals(true, filter.test(testFileContent2))
    }

    @Test
    fun test_simple_inclusions() {
        val filter = ExpressionFileFilterSupplier.expressions(
            inclusions = listOf("file.name == '1.txt'")
        )
        val testFileContent1 = FixedFileContent(Path("1.txt"))
        assertEquals(true, filter.test(testFileContent1))
        val testFileContent2 = FixedFileContent(Path("2.txt"))
        assertEquals(false, filter.test(testFileContent2))
    }

    @Test
    fun test_multiple() {
        val filter = ExpressionFileFilterSupplier.expressions(
            exclusions = listOf(
                "file.attrs.size > 1024*1024",
                "file.name.matches('.*qaz.*')"
            ),
            inclusions = listOf(
                "file.attrs.size < 1024*1024",
                "file.name.matches('.*Test.*')"
            ),
        )

        val path = Path(
            "src",
            "test",
            "kotlin",
            "io",
            "github",
            "shoaky",
            "sourcedownloader",
            "component",
            "ExpressionFileFilterTest.kt"
        )
        assertEquals(
            true, filter.test(
            FixedFileContent(path, attrs = mapOf("size" to path.fileSize())),
            )
        )
    }

    @Test
    fun test_all_variables() {
        val filter = ExpressionFileFilter(
            inclusions = listOf(
                """
                file.name.contains('test') && 
                'video' in file.tags &&
                file.extension == 'txt' &&
                file.vars.test == 'test' &&
                file.attrs.size > 10 &&
                'book' in file.paths
            """.trimIndent()
            )
        )
        val downloadPath = testResourcePath.resolve("downloads")
        val testFileContent = FixedFileContent(
            fileDownloadPath = downloadPath.resolve(Path("book", "test.txt")),
            downloadPath = downloadPath,
            tags = setOf("video"),
            patternVariables = MapPatternVariables(mapOf("test" to "test")),
            attrs = mapOf("size" to 100)
        )
        assertEquals(true, filter.test(testFileContent))
    }

    @Test
    fun test_lib_containsAny() {
        val filter1 = ExpressionFileFilter(
            exclusions = listOf(
                """
                file.paths.containsAny(['SPs'])
            """.trimIndent()
            )
        )

        val downloadPath = testResourcePath.resolve("downloads")
        val file1 = FixedFileContent(
            fileDownloadPath = downloadPath.resolve(Path("SPs", "test.txt")),
            downloadPath = downloadPath,
            tags = setOf("video"),
        )
        assertEquals(false, filter1.test(file1))
        val file2 = file1.copy(fileDownloadPath = downloadPath.resolve(Path("sps", "test.txt")))
        assertEquals(true, filter1.test(file2))
    }

    @Test
    fun test_lib_containsAny_given_ignore_case() {
        val filter1 = ExpressionFileFilter(
            exclusions = listOf(
                """
                file.paths.containsAny(['sp', 'sps', 'extra'], true)
            """.trimIndent()
            )
        )

        val downloadPath = testResourcePath.resolve("downloads")
        val file1 = FixedFileContent(
            fileDownloadPath = downloadPath.resolve(Path("SP", "test.txt")),
            downloadPath = downloadPath,
            tags = setOf("video"),
        )
        assertEquals(false, filter1.test(file1))
        val file2 = file1.copy(fileDownloadPath = downloadPath.resolve(Path("ddd", "test.txt")))
        assertEquals(true, filter1.test(file2))
    }
}