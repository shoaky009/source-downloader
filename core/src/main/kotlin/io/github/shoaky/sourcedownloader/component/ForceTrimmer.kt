package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.component.Trimmer
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

object ForceTrimmer : Trimmer {

    override fun trim(value: String, expectSize: Int): String {
        val decoder = Charsets.UTF_8.newDecoder()
        decoder.onMalformedInput(CodingErrorAction.IGNORE)
        decoder.reset()
        val buf = ByteBuffer.wrap(value.toByteArray(), 0, expectSize)
        val decode = decoder.decode(buf)
        return decode.toString()
    }
}