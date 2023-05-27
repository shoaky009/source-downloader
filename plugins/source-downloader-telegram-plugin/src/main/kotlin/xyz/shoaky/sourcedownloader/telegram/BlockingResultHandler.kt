package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.GenericResultHandler
import it.tdlight.client.Result
import it.tdlight.jni.TdApi
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class BlockingResultHandler<T: TdApi.Object>(
    timeout: Long = 10000
) : GenericResultHandler<T> {

    val future: CompletableFuture<Result<T>> = CompletableFuture<Result<T>>()
        .orTimeout(timeout, TimeUnit.MILLISECONDS)

    override fun onResult(result: Result<T>) {
        future.complete(result)
    }
}