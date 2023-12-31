package io.github.shoaky.sourcedownloader.foreign.grpc

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import io.grpc.ManagedChannelBuilder

class GrpcForeignStateClient : ForeignStateClient {

    private val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
    // private val stub = ForeignStateServiceGrpc.newBlockingStub(channel)

    override fun <T : Any> postState(path: String, state: Any, typeReference: TypeReference<T>): T {
        TODO()
    }

    override fun <T : Any> getState(path: String, typeReference: TypeReference<T>): T {
        TODO()
    }
}
