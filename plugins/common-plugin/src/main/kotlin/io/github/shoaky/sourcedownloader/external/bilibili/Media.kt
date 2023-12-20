package io.github.shoaky.sourcedownloader.external.bilibili

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

data class Media(
    val id: Long,
    val type: Int,
    val title: String,
    val cover: URI,
    val page: Int,
    val duration: Long,
    val upper: Upper,
    val attr: Int,
    @JsonProperty("cnt_info")
    val cntInfo: CntInfo,
    val link: URI,
    val ctime: Long,
    val pubtime: Long,
    @JsonProperty("fav_time")
    val favTime: Long,
    @JsonProperty("bv_id")
    val bvId: String
)

data class Upper(
    val minId: Long,
    val name: String,
    val face: URI,
)

data class CntInfo(
    val collect: Long,
    val play: Long,
    val danmaku: Long,
)