package xyz.shoaky.sourcedownloader.telegram

import it.tdlight.client.GenericResultHandler
import it.tdlight.client.Result
import it.tdlight.jni.TdApi
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class BlockingResultHandler<T : TdApi.Object>(
    timeout: Long = 10000
) : GenericResultHandler<T> {

    val future: CompletableFuture<Result<T>> = CompletableFuture<Result<T>>()

    init {
        if (timeout > 0) {
            future.orTimeout(timeout, TimeUnit.MILLISECONDS)
        }
    }

    override fun onResult(result: Result<T>) {
        if (result.isError) {
            future.completeExceptionally(RuntimeException(result.error.message))
        } else {
            future.complete(result)
        }
    }

    fun get(): T {
        return future.get().get()
    }
}