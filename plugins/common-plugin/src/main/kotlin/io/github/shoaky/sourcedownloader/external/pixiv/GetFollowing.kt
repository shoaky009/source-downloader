package io.github.shoaky.sourcedownloader.external.pixiv

data class GetFollowing(
    val users: List<PixivUser> = emptyList(),
    val total: Int
)
