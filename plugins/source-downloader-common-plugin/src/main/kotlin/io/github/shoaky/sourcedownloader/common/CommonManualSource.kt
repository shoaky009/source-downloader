package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.sdk.SourceItemConvertScript
import io.github.shoaky.sourcedownloader.sdk.component.ManualSource
import java.net.URL

object CommonManualSource : ManualSource {

    override fun getScript(url: URL): SourceItemConvertScript? {
        if (url.host == "v2ex.com" && url.path.startsWith("/t/").not()) {
            return SourceItemConvertScript(
                ".item",
                "return item.querySelector('item_title').textContent",
                "return item.querySelector('.item_title a').href",
                "return item.querySelector('.topic_info [title]').title",
                "return 'html'",
            )
        }
        return null
    }

}