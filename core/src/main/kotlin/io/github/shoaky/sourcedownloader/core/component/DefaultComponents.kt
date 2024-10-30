package io.github.shoaky.sourcedownloader.core.component

class DefaultComponents : ComponentConfigStorage {

    private val componentConfig: Map<String, List<ComponentConfig>> by lazy {
        mapOf(
            "trigger" to listOf(
                ComponentConfig("30min", "fixed", mapOf("interval" to "PT30M")),
                ComponentConfig("1hour", "fixed", mapOf("interval" to "PT1H")),
                ComponentConfig("2hour", "fixed", mapOf("interval" to "PT2H")),
                ComponentConfig("3hour", "fixed", mapOf("interval" to "PT3H")),
                ComponentConfig("6hour", "fixed", mapOf("interval" to "PT6H")),
                ComponentConfig("12hour", "fixed", mapOf("interval" to "PT12H")),
                ComponentConfig("1day", "fixed", mapOf("interval" to "P1D")),
            ),
        )
    }

    override fun getAllComponentConfig(): Map<String, List<ComponentConfig>> {
        return componentConfig
    }
}