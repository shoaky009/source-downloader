package io.github.shoaky.sourcedownloader.foreign.native

import com.fasterxml.jackson.core.type.TypeReference
import io.github.shoaky.sourcedownloader.foreign.ForeignStateClient
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_CHAR_UNALIGNED

class NativeForeignStateClient : ForeignStateClient {

    override fun <T : Any> postState(path: String, state: Any, typeReference: TypeReference<T>): T {
        val nativeLinker = Linker.nativeLinker()
        val stdlibLookup: SymbolLookup = nativeLinker.defaultLookup()
        val loaderLookup = SymbolLookup.loaderLookup()
        val address = ValueLayout.ADDRESS
        val printfDescriptor: FunctionDescriptor = FunctionDescriptor.of(JAVA_CHAR_UNALIGNED, address)
        val get = loaderLookup.find("printf").get()
        TODO("Not yet implemented")
    }

    override fun <T : Any> getState(path: String, typeReference: TypeReference<T>): T {
        TODO("Not yet implemented")
    }
}