package io.github.shoaky.sourcedownloader.nativeimage

import org.graalvm.nativeimage.hosted.Feature

@Suppress("UNUSED")
class CommonPluginFeature : Feature {

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        // 预留
    }
}