package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class SupportingRequest : FanboxRequest<List<Supporting>>() {

    override val path: String = "/plan.listSupporting"
    override val responseBodyType: TypeReference<FanboxResponse<List<Supporting>>> = jacksonTypeRef()

}