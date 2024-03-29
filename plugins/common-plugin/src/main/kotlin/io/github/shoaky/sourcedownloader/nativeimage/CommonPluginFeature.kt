package io.github.shoaky.sourcedownloader.nativeimage

import io.github.shoaky.sourcedownloader.common.anime.MikanPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection

@Suppress("UNUSED")
class CommonPluginFeature : Feature {

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        // 预留
        RuntimeReflection.registerAllDeclaredConstructors(MikanPointer::class.java)
        println("==========Common beforeAnalysis===========")

        access.registerSubtypeReachabilityHandler({ a, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
        }, SourcePointer::class.java)
    }
}