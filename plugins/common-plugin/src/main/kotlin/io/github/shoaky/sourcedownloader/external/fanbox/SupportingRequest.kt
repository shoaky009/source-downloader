package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class SupportingRequest : FanboxRequest<SupportingBody>() {

    override val path: String = "/plan.listSupporting"
    override val responseBodyType: TypeReference<FanboxResponse<SupportingBody>> = jacksonTypeRef()

}

data class SupportingBody(
    val plans: List<Supporting>
)