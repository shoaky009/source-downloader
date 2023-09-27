package io.github.shoaky.sourcedownloader.nativeimage

import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection

@Suppress("UNUSED")
class CoreFeature : Feature {

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        // 预留
        println("==========Core BeforeAnalysis===========")

        access.registerSubtypeReachabilityHandler({ _, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
        }, SourcePointer::class.java)
        access.registerSubtypeReachabilityHandler({ _, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
        }, ItemPointer::class.java)
    }
}