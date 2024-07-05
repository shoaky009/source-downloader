package io.github.shoaky.sourcedownloader.common

import io.github.shoaky.sourcedownloader.common.anime.BgmTvClientInstanceFactory
import io.github.shoaky.sourcedownloader.common.anime.MikanSupportFactory
import io.github.shoaky.sourcedownloader.common.supplier.*
import io.github.shoaky.sourcedownloader.sdk.plugin.Plugin
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginDescription

internal class CommonPlugin : Plugin {

    override fun init(pluginContext: PluginContext) {
        pluginContext.registerInstanceFactory(
            QbittorrentClientInstanceFactory,
            BgmTvClientInstanceFactory,
            MikanSupportFactory
        )
        pluginContext.registerSupplier(
            AnimeFileFilterSupplier,
            AnimeReplacementDeciderSupplier,
            AnimeTaggerSupplier,
            AnimeVariableProviderSupplier(pluginContext),
            AnitomVariableProviderSupplier,
            BbDownIntegrationSupplier,
            BgmTvVariableProviderSupplier(pluginContext),
            BilibiliSourceSupplier,
            // CommonManualSourceSupplier,
            DlsiteVariableProviderSupplier,
            EmbyImageTaggerSupplier,
            EpisodeVariableProviderSupplier,
            FanboxIntegrationSupplier,
            GetchuVariableProviderSupplier,
            HtmlFileResolverSupplier,
            JackettSourceSupplier,
            KeywordVariableProviderSupplier,
            LanguageVariableProviderSupplier,
            MediaTypeExistsDetectorSupplier,
            MikanSourceSupplier(pluginContext),
            MikanVariableProviderSupplier(pluginContext),
            OpenAiVariableProviderSupplier,
            PatreonIntegrationSupplier,
            PixivIntegrationSupplier,
            QbittorrentDownloaderSupplier(pluginContext.getInstanceManager()),
            ResolutionVariableProviderSupplier,
            RssSourceSupplier,
            SeasonVariableProviderSupplier,
            SimpleFileTaggerSupplier,
            TmdbVariableProviderSupplier,
            TorrentFileResolverSupplier,
            TransmissionDownloaderSupplier,
            WebdavMoverSupplier,
            YoutubeDLIntegrationSupplier,
            ChiiVariableProviderSupplier
        )
    }

    override fun destroy(pluginContext: PluginContext) {

    }

    override fun description(): PluginDescription {
        return PluginDescription("common", "0.0.1")
    }

}