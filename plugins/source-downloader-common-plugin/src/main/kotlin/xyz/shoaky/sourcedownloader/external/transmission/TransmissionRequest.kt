package xyz.shoaky.sourcedownloader.external.transmission

import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import xyz.shoaky.sourcedownloader.sdk.util.http.BodyMapper
import xyz.shoaky.sourcedownloader.sdk.util.http.CommonBodyHandler
import xyz.shoaky.sourcedownloader.sdk.util.http.MappingInfo

abstract class TransmissionRequest<R : Any> : BaseRequest<TransmissionResponse<R>>() {

    override val path: String = "/transmission/rpc"
    override val httpMethod: HttpMethod = HttpMethod.POST
    override val mediaType: MediaType = MediaType.JSON_UTF_8

    val tag = System.currentTimeMillis()
    abstract val method: String
    abstract val arguments: Map<String, Any?>

    override fun bodyHandler(): CommonBodyHandler<TransmissionResponse<R>> {
        val bodyHandler = super.bodyHandler()
        bodyHandler.addBodyMapper("html"
        ) {
            object : BodyMapper<TransmissionResponse<R>> {
                override fun mapping(info: MappingInfo<TransmissionResponse<R>>): TransmissionResponse<R> {
                    // transmission rpc返回html代表有错误, 给错误的结果
                    return Jackson.fromJson("""{"result":"csrf", "tag": $tag, "arguments":{}}""", info.type)
                }
            }
        }
        return bodyHandler
    }
}