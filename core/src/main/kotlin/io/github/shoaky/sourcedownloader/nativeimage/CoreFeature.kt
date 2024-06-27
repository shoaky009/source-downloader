package io.github.shoaky.sourcedownloader.nativeimage

import io.github.shoaky.sourcedownloader.core.AllDeclaredConfig
import io.github.shoaky.sourcedownloader.core.InstanceConfig
import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentConfig
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection

@Suppress("UNUSED")
class CoreFeature : Feature {

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        println("==========Core BeforeAnalysis===========")

        access.registerSubtypeReachabilityHandler({ _, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
        }, SourcePointer::class.java)
        access.registerSubtypeReachabilityHandler({ _, v ->
            RuntimeReflection.registerAllDeclaredConstructors(v)
        }, ItemPointer::class.java)

        RuntimeReflection.registerAllDeclaredConstructors(AllDeclaredConfig::class.java)
        RuntimeReflection.registerAllDeclaredConstructors(InstanceConfig::class.java)
        RuntimeReflection.registerAllDeclaredConstructors(ProcessorConfig::class.java)
        RuntimeReflection.registerAllDeclaredConstructors(ProcessorConfig.Options::class.java)
        RuntimeReflection.registerAllDeclaredConstructors(ComponentConfig::class.java)
    }
}