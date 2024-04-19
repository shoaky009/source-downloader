package io.github.shoaky.sourcedownloader.sdk.util.http

import java.net.http.HttpHeaders

class BodyMappingException(
    val body: String,
    val statusCode: Int,
    val headers: HttpHeaders,
    message: String,
) : RuntimeException(message)