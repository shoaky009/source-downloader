package io.github.shoaky.sourcedownloader.application.spring

import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

class ComponentFailureAnalyzer : AbstractFailureAnalyzer<ComponentException>() {

    override fun analyze(rootFailure: Throwable, cause: ComponentException): FailureAnalysis {
        return FailureAnalysis(
            "Component failed to create",
            cause.message,
            cause
        )
    }
}