package io.github.shoaky.sourcedownloader.external.bbdown

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.common.bilibili.BbDownOptions
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest

data class AddTask(
    @JsonProperty("Url")
    val url: String,
    @JsonProperty("Cookie")
    val cookie: String? = null,
    @JsonProperty("FilePattern")
    val filePattern: String? = null,
    @JsonProperty("WorkDir")
    val workDir: String? = null,
    @JsonProperty("SelectPage")
    val selectPage: String? = null,
    @JsonProperty("MultiFilePattern")
    val multiFilePattern: String? = null,
    @JsonProperty("DfnPriority")
    val dfnPriority: String? = null,
    @JsonProperty("DownloadDanmaku ")
    val downloadDanmaku: Boolean? = null,
) : BaseRequest<String>() {

    constructor(url: String, options: BbDownOptions) : this(
        url = url,
        cookie = options.cookie,
        filePattern = options.filePattern,
        workDir = options.workDir,
        selectPage = options.selectPage,
        multiFilePattern = options.multiFilePattern,
        dfnPriority = options.dfnPriority,
        downloadDanmaku = options.downloadDanmaku
    )

    override val path: String = "/add-task"
    override val responseBodyType: TypeReference<String> = jacksonTypeRef()
    override val httpMethod: String = "POST"
    override val mediaType: MediaType? = MediaType.JSON_UTF_8
}