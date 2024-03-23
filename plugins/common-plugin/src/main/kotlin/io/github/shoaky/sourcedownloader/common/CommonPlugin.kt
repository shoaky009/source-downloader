package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.anime.BgmTvClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.anime.MikanSupportFactory
import io.github.shoaky.sourcedownloader.common.supplier.*
import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.torrent.QbittorrentDownloaderSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginDescription
import io.github.shoaky.sourcedownloader.sdk.util.getObjectSuppliers

internal class CommonPlugin : Plugin {

    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(
            QbittorrentClientInstanceFactory,
            BgmTvClientInstanceFactory,
            MikanSupportFactory
        )
        pluginContext.registerSupplier(
            *getObjectSuppliers("io.github.shoaky.sourcedownloader.common.supplier"),
            // *getObjectSupplier(),
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
            AnimeVariableProviderSupplier(pluginContext),
            MikanVariableProviderSupplier(pluginContext),
            MikanSourceSupplier(pluginContext),
            BgmTvVariableProviderSupplier(pluginContext)
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }

    private fun getObjectSupplier(): Array<ComponentSupplier<*>> {
        return arrayOf(
            AnimeFileFilterSupplier,
            AnimeReplacementDeciderSupplier,
            AnimeTaggerSupplier,
            AnitomVariableProviderSupplier,
            CommonManualSourceSupplier,
            DlsiteVariableProviderSupplier,
            EpisodeVariableProviderSupplier,
            FanboxIntegrationSupplier,
            JackettSourceSupplier,
            LanguageVariableProviderSupplier,
            MediaTypeExistsDetectorSupplier,
            OpenAiVariableProviderSupplier,
            PatreonIntegrationSupplier,
            RssSourceSupplier,
            SeasonVariableProviderSupplier,
            SimpleFileTaggerSupplier,
            TorrentFileResolverSupplier,
            TransmissionDownloaderSupplier,
            WebdavMoverSupplier,
            PixivIntegrationSupplier,
            KeywordVariableProviderSupplier,
            BilibiliSourceSupplier,
            EmbyImageTaggerSupplier
        )
    }
}