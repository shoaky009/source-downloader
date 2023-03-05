package xyz.shoaky.sourcedownloader.core.component

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sourceItem
import kotlin.io.path.Path
import kotlin.test.assertEquals

class JavaScriptContentCreatorTest {

    @Test
    fun should_vars_expected_values() {
        val props = ComponentProps.fromMap(
            mapOf(
                "script" to """
                console.log(item);
                vars["title"] = item.title;
                vars["path"] = path;
            """
            ))
        val creator = ScriptContentCreatorSupplier.apply(props)

        val group = creator.createSourceGroup(sourceItem("source-test-title"))
        val path = listOf(Path("test"))
        group.sourceFiles(path)
            .forEach {
                assertEquals("source-test-title", it.patternVars().getVar("title"))
                assertEquals("test", it.patternVars().getVar("path"))
            }
    }
}