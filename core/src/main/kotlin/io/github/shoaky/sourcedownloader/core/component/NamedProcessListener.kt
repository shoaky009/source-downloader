package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener

class NamedProcessListener(
    val id: ComponentId,
    val component: ProcessListener
) : ProcessListener by component {

    override fun toString(): String {
        return id.toString()
    }
}