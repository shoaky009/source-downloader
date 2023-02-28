package xyz.shoaky.sourcedownloader.mikan

import xyz.shoaky.sourcedownloader.sdk.*

class MikanPlugin : Plugin {
    override fun init(pluginContext: PluginContext) {
        pluginContext.registerSupplier(MikanCreatorSupplier)

        val patternVarsDescription = PatternVarsDescription(
            MikanCreatorSupplier.availableTypes().first(),
            listOf(
                VarDescription("mikan-title", true, "Mikan的标题，中文的"),
                VarDescription("name", true, "番剧原标题多数为日文，bgm.tv上的"),
                VarDescription("name-cn", true, "番剧中文标题，bgm.tv上的"),
                VarDescription("date", true, "番剧开播日期，yyyy-MM-dd"),
                VarDescription("year", true, "番剧开播年份"),
                VarDescription("month", true, "番剧开播月份"),
                VarDescription("season", true, "季数字"),
                VarDescription("episode", true, "集数字"),
                VarDescription("origin-filename", true, "原文件名"),
            )
        )
        pluginContext.addPatternVarsDescription(patternVarsDescription)
    }

    override fun destroy(pluginContext: PluginContext) {
        println("Mikan plugin destroy")
    }

    override fun description(): PluginDescription {
        return PluginDescription("Mikan", "1.0.0")
    }

}