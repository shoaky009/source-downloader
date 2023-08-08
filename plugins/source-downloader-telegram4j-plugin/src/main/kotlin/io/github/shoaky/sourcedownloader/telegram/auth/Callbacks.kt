package io.github.shoaky.sourcedownloader.telegram.auth

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import reactor.core.publisher.Mono
import telegram4j.core.auth.AuthorizationHandler
import telegram4j.core.auth.QRAuthorizationHandler
import telegram4j.core.auth.TwoFactorHandler
import telegram4j.core.auth.TwoFactorHandler.Callback.ActionType
import java.util.*

object QRCallback : Base2FACallback(), QRAuthorizationHandler.Callback {

    override fun onLoginToken(res: AuthorizationHandler.Resources, ctx: QRAuthorizationHandler.Context): Mono<ActionType> {
        return Mono.fromSupplier {
            ctx.log("New QR code (you have " + ctx.expiresIn().toSeconds() + " seconds to scan it)")
            println(generateQr(ctx.loginUrl()))
            ActionType.RETRY
        }
    }

    private fun generateQr(text: String): String? {
        val width = 40
        val height = 40
        val qrParam: Hashtable<EncodeHintType, Any> = Hashtable()
        qrParam[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        qrParam[EncodeHintType.CHARACTER_SET] = "utf-8"
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam)
            toAscii(bitMatrix)
        } catch (ex: WriterException) {
            throw java.lang.IllegalStateException("Can't encode QR code", ex)
        }
    }

    private fun toAscii(bitMatrix: BitMatrix): String {
        val sb = StringBuilder()
        for (rows in 0 until bitMatrix.height) {
            for (cols in 0 until bitMatrix.width) {
                val x = bitMatrix[rows, cols]
                sb.append(if (x) "  " else "██")
            }
            sb.append("\n")
        }
        return sb.toString()
    }


}

open class Base2FACallback : TwoFactorHandler.Callback {

    protected val sc: Scanner = Scanner(System.`in`)
    override fun on2FAPassword(res: AuthorizationHandler.Resources, ctx: TwoFactorHandler.Context): Mono<String> {
        return Mono.fromCallable {
            var base = "The account is protected by 2FA, please write password"
            val hint = ctx.srp().hint()
            if (hint != null) {
                base += " (Hint: '$hint')"
            }
            ctx.log(base)
            sc.nextLine()
        }
    }
}