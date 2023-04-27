package xyz.shoaky.sourcedownloader.core

import org.junit.jupiter.api.Test
import xyz.shoaky.sourcedownloader.sdk.InstanceFactory
import xyz.shoaky.sourcedownloader.sdk.Properties
import kotlin.io.path.Path
import kotlin.test.assertEquals

class DefaultInstanceManagerTest {

    private val instanceManager = DefaultInstanceManager(
        YamlConfigStorage(Path("src/test/resources/config-test2.yaml"))
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
}

class TestInstance(
    val appId: Int
)