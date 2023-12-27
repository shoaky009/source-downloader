package io.github.shoaky.sourcedownloader.external.webdav

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.http.BaseRequest
import java.net.http.HttpRequest
import java.nio.file.Path

abstract class WebdavReq<T : Any> : BaseRequest<T>()

class UploadFile(
    override val path: String,
    private val file: Path
) : WebdavReq<String>() {

    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: String = "PUT"
    override val mediaType: MediaType? = null

    override fun bodyPublisher(): HttpRequest.BodyPublisher? {
        return HttpRequest.BodyPublishers.ofFile(file)
    }

}

class MoveFile(
    src: String,
    dst: String,
) : WebdavReq<String>() {

    override val path: String = src
    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: String = "MOVE"
    override val mediaType: MediaType? = null

    init {
        setHeader("Destination", dst)
    }

    override fun bodyPublisher(): HttpRequest.BodyPublisher? {
        return HttpRequest.BodyPublishers.noBody()
    }
}

class CreateDirectory(
    override val path: String,
) : WebdavReq<String>() {

    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: String = "MKCOL"
    override val mediaType: MediaType? = null

    override fun bodyPublisher(): HttpRequest.BodyPublisher? {
        return HttpRequest.BodyPublishers.noBody()
    }
}

class FindProp(
    override val path: String,
) : WebdavReq<String>() {

    override val responseBodyType: TypeReference<String> = stringTypeReference
    override val httpMethod: String = "PROPFIND"
    override val mediaType: MediaType? = null

    override fun bodyPublisher(): HttpRequest.BodyPublisher? {
        return HttpRequest.BodyPublishers.noBody()
    }
}