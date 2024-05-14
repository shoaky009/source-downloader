package io.github.shoaky.sourcedownloader.sdk.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentTypeTest {
    @Test
    fun given_mover_type_should_expected() {
        val type = ComponentType.fileMover("mikan")
        val fullName = type.fullName()
        assertEquals("file-mover:mikan", fullName)
        val instanceName = type.instanceName("mine")
        assertEquals("$fullName:mine", instanceName)
    }
}