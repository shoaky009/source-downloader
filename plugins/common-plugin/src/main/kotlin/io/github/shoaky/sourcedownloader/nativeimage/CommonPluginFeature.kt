package io.github.shoaky.sourcedownloader.nativeimage

import io.github.shoaky.sourcedownloader.common.rss.RssConfig
import io.github.shoaky.sourcedownloader.external.qbittorrent.QbittorrentConfig
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection

@Suppress("UNUSED")
class CommonPluginFeature : Feature {

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        println("==========Common beforeAnalysis===========")
        access.registerSubtypeReachabilityHandler({ _, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
            RuntimeReflection.registerAllDeclaredFields(v)
            RuntimeReflection.registerAllDeclaredMethods(v)
        }, SourcePointer::class.java)
        RuntimeReflection.registerAllDeclaredConstructors(QbittorrentConfig::class.java)
        RuntimeReflection.registerAllDeclaredFields(QbittorrentConfig::class.java)
        RuntimeReflection.registerForReflectiveInstantiation(RssConfig::class.java)
    }
}