package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.component.DefaultInstanceManager
import io.github.shoaky.sourcedownloader.sdk.InstanceFactory
import io.github.shoaky.sourcedownloader.sdk.Properties
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.test.assertEquals

class DefaultInstanceManagerTest {

    private val instanceManager = DefaultInstanceManager(
        YamlConfigStorage(Path("src", "test", "resources", "config-test2.yaml"))
    )

    init {
        instanceManager.registerInstanceFactory(TestFactory)
    }

    @Test
    fun test() {
        val instance = instanceManager.load("client1", TestInstance::class.java)
        assertEquals(1111, instance.appId)
        val instance2 = instanceManager.load("client1", TestInstance::class.java)
        assert(instance === instance2)
    }
}

object TestFactory : InstanceFactory<TestInstance> {
    override fun create(props: Properties): TestInstance {
        return TestInstance(props.get<Int>("appId"))
    }

    override fun type(): Class<TestInstance> {
        return TestInstance::class.java
    }
}

class TestInstance(
    val appId: Int
)