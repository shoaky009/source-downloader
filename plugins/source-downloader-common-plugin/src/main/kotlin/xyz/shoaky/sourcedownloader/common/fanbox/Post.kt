package xyz.shoaky.sourcedownloader.common.fanbox

internal data class Post(
    val id: String,
    val title:String,
    val creatorId:String,
    val user: User,
)