package xyz.shoaky.sourcedownloader.external.transmission

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class SessionGet : TransmissionRequest<Session>() {
    override val method: String = "session-get"
    override val arguments: Map<String, Any> = mapOf()
    override val responseBodyType: TypeReference<TransmissionResponse<Session>> = jacksonTypeRef()
}