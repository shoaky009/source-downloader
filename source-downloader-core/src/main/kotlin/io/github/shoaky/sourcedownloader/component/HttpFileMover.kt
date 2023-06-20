package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.util.encodeBase64
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path

open class HttpFileMover(
    private val serverUrl: String,
    private val username: String? = null,
    private val password: String? = null
) : FileMover {
    override fun move(sourceContent: SourceContent): Boolean {
        // 后面异步
        val responses = sourceContent.sourceFiles.map {
            val createFile = createFile(it.fileDownloadPath, it.targetPath())
            if (createFile.statusCode() != HttpStatus.CREATED.value()) {
                log.error("Failed to create file: ${it.targetPath()}")
            }
            createFile
        }
        return responses.all { it.statusCode() == HttpStatus.CREATED.value() }
    }

    private fun createFile(filePath: Path, targetPath: Path): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI(serverUrl + targetPath))
            .expectContinue(true)
        if (username != null && password != null) {
            val authorization = "$username:$password".encodeBase64()
            builder.header(HttpHeaders.AUTHORIZATION, "Basic $authorization")
        }
        builder.PUT(HttpRequest.BodyPublishers.ofFile(filePath))
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

}