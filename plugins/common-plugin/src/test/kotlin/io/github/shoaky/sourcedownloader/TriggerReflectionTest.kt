package io.github.shoaky.sourcedownloader

import io.github.shoaky.sourcedownloader.common.anime.FansubPointer
import io.github.shoaky.sourcedownloader.common.anime.MikanPointer
import io.github.shoaky.sourcedownloader.common.fanbox.CreatorPointer
import io.github.shoaky.sourcedownloader.common.fanbox.FanboxPointer
import io.github.shoaky.sourcedownloader.common.patreon.CampaignPointer
import io.github.shoaky.sourcedownloader.common.patreon.PatreonPointer
import io.github.shoaky.sourcedownloader.common.rss.RssConfig
import io.github.shoaky.sourcedownloader.external.anilist.PageResponse
import io.github.shoaky.sourcedownloader.external.anilist.Search
import io.github.shoaky.sourcedownloader.external.bangumi.*
import io.github.shoaky.sourcedownloader.external.fanbox.*
import io.github.shoaky.sourcedownloader.external.openai.ChatResponse
import io.github.shoaky.sourcedownloader.external.patreon.*
import io.github.shoaky.sourcedownloader.external.qbittorrent.*
import io.github.shoaky.sourcedownloader.external.qbittorrent.TorrentFile
import io.github.shoaky.sourcedownloader.external.transmission.*
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.instancio.Instancio
import org.instancio.TypeToken
import org.instancio.TypeTokenSupplier
import org.junit.jupiter.api.Test

class TriggerReflectionTest {

    @Test
    fun type() {
        val types = listOf(
            typeOf<Search>(),
            typeOf<PageResponse>(),
            typeOf<SearchSubjectV0Body>(),
            typeOf<SearchSubjectBody>(),
            typeOf<GetSubjectRequest>(),
            typeOf<SearchSubjectRequest>(),
            typeOf<SearchSubjectV0Request>(),
            typeOf<CreatorPaginateRequest>(),
            typeOf<CreatorPostsRequest>(),
            typeOf<SupportingPostsRequest>(),
            typeOf<PostInfoRequest>(),
            typeOf<SupportingRequest>(),
            typeOf<HomePostsRequest>(),
            typeOf<ChatResponse>(),
            typeOf<PostRequest>(),
            typeOf<PledgeRequest>(),
            typeOf<PostsRequest>(),
            typeOf<PostTagRequest>(),
            typeOf<TorrentInfoRequest>(),
            typeOf<TorrentPropertiesRequest>(),
            typeOf<TorrentsRenameFileRequest>(),
            typeOf<TorrentsSetLocationRequest>(),
            typeOf<TorrentDeleteRequest>(),
            typeOf<TorrentFilePrioRequest>(),
            typeOf<TorrentFilesRequest>(),
            typeOf<TorrentsAddRequest>(),
            typeOf<TestCsrfRequest>(),
            typeOf<TorrentAdd>(),
            typeOf<TorrentDelete>(),
            typeOf<TorrentGet>(),
            typeOf<TorrentRenamePath>(),
            typeOf<TorrentSet>(),
            typeOf<TorrentSetLocation>(),
            typeOf<PledgeResponse>(),
            typeOf<PostsResponse>(),
            typeOf<LoginRequest>(),
            typeOf<TorrentProperties>(),
            typeOf<List<TorrentFile>>(),
            typeOf<PatreonResponse<List<PatreonEntity<PostTag>>>>(),
            typeOf<PostResponse>(),
            typeOf<TorrentGetResponse>(),
            typeOf<TransmissionResponse<TorrentGetResponse>>(),
            typeOf<TransmissionResponse<Session>>(),
            typeOf<AppGetDefaultSavePathRequest>(),
            typeOf<RssConfig>(),
            typeOf<QbittorrentConfig>(),
            typeOf<PatreonPointer>(),
            typeOf<CampaignPointer>(),
            typeOf<FanboxPointer>(),
            typeOf<CreatorPointer>(),
            typeOf<MikanPointer>(),
            typeOf<FansubPointer>(),
        )

        for (type in types) {
            println(Jackson.toJsonString(Instancio.create(type)))
        }
    }

}

inline fun <reified T> typeOf(): TypeTokenSupplier<T> {
    return object : TypeToken<T> {}
}