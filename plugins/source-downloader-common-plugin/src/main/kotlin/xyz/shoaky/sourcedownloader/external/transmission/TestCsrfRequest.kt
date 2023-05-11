package xyz.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

internal class TestCsrfRequest : TransmissionRequest<Any>() {
    override val method: String = "test"
    override val arguments: Map<String, Any> = emptyMap()
    override val responseBodyType: TypeReference<TransmissionResponse<Any>> = jacksonTypeRef()
}