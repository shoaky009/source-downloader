package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.HttpDownloader
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import java.net.http.HttpClient
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object HttpDownloaderSupplier : ComponentSupplier<HttpDownloader> {

    override fun apply(context: CoreContext, props: Properties): HttpDownloader {
        val path = props.get<Path>("download-path")
        val p = props.getOrDefault<Int>("parallelism", 5)
        val hosts = props.getOrDefault<Set<String>>("cert-validation-bypass-hosts", emptySet())
        val c = if (hosts.isEmpty()) {
            httpClient
        } else {
            val sslContext = SSLContext.getInstance("TLS")
            val tm = SelectiveTrustManager(hosts)
            sslContext.init(null, arrayOf<TrustManager>(tm), null)
            HttpClient.newBuilder()
                .sslContext(sslContext)
                .build()
        }
        return HttpDownloader(path, c, p)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("http")
        )
    }
}

class SelectiveTrustManager(
    private val allowedHosts: Set<String>
) : X509TrustManager {

    private val defaultTm: X509TrustManager = getDefaultX509TrustManager()

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String?) {
        if (chain.isEmpty()) {
            defaultTm.checkServerTrusted(chain, authType)
        }
        val cn = chain[0].getSubjectX500Principal().name
        val isAllowed = allowedHosts.stream().anyMatch { it == null || cn.contains(it) }
        if (isAllowed) {
            return
        }
        defaultTm.checkServerTrusted(chain, authType)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
        return defaultTm.acceptedIssuers
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String?) {
        defaultTm.checkClientTrusted(chain, authType)
    }

    companion object {

        private fun getDefaultX509TrustManager(): X509TrustManager {
            val factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            factory.init(null as KeyStore?)

            for (tm in factory.trustManagers) {
                if (tm is X509TrustManager) {
                    return tm
                }
            }
            throw IllegalStateException("No default X509TrustManager found")
        }
    }
}