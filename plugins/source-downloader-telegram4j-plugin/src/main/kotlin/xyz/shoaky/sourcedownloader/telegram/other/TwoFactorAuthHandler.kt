package xyz.shoaky.sourcedownloader.telegram.other

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import reactor.core.publisher.Mono
import telegram4j.mtproto.DcId
import telegram4j.mtproto.client.MTProtoClientGroup
import telegram4j.mtproto.util.CryptoUtil.*
import telegram4j.tl.ImmutableBaseInputCheckPasswordSRP
import telegram4j.tl.PasswordKdfAlgoSHA256SHA256PBKDF2HMACSHA512iter100000SHA256ModPow
import telegram4j.tl.account.Password
import telegram4j.tl.auth.Authorization
import telegram4j.tl.request.account.GetPassword
import telegram4j.tl.request.auth.ImmutableCheckPassword
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey


class TwoFactorAuthHandler(
    private val clientGroup: MTProtoClientGroup,
) {

    private val sc: Scanner = Scanner(System.`in`)
    private var first2fa = true

    // Ported version of https://gist.github.com/andrew-ld/524332536dbc8c525ed80d281855a0d4 and
    // https://github.com/DrKLO/Telegram/blob/abb896635f849a93968a2ba35a944c91b4978be4/TMessagesProj/src/main/java/org/telegram/messenger/SRPHelper.java#L29
    fun begin2FA(): Mono<Authorization> {
        return clientGroup.send<Password, GetPassword>(DcId.main(), GetPassword.instance()).flatMap { pswrd: Password ->
            if (!pswrd.hasPassword()) {
                return@flatMap Mono.error<Authorization>(IllegalStateException("?".repeat(1 shl 4)))
            }
            val currentAlgo = pswrd.currentAlgo()
            if (currentAlgo !is PasswordKdfAlgoSHA256SHA256PBKDF2HMACSHA512iter100000SHA256ModPow) {
                return@flatMap Mono.error<Authorization>(IllegalStateException("Unexpected type of current algorithm: $currentAlgo"))
            }
            synchronized(System.out) {
                println(CodeAuthorization.delimiter)
                if (first2fa) {
                    first2fa = false
                    print("The account is protected by 2FA, please write password")
                } else {
                    print("Invalid password, please write it again")
                }
                val hint = pswrd.hint()
                if (hint != null) {
                    print(" (Hint \"$hint\")")
                }
                print(": ")
            }
            val password = sc.nextLine()
            val g = BigInteger.valueOf(currentAlgo.g().toLong())
            val gBytes = toBytesPadded(g)
            val pBytes = currentAlgo.p()
            val salt1 = currentAlgo.salt1()
            val salt2 = currentAlgo.salt2()
            val k = fromByteBuf(sha256Digest(pBytes, gBytes))
            val p = fromByteBuf(pBytes.retain())
            val hash1 = sha256Digest(salt1, Unpooled.wrappedBuffer(password.toByteArray(StandardCharsets.UTF_8)), salt1)
            val hash2 = sha256Digest(salt2, hash1, salt2)
            val hash3 = pbkdf2HmacSha512Iter100000(hash2, salt1)
            val x = fromByteBuf(sha256Digest(salt2, hash3, salt2))
            random.setSeed(toByteArray(pswrd.secureRandom()))
            val a = random2048Number()
            val gA = g.modPow(a, p)
            val gABytes = toBytesPadded(gA)
            val srpB = pswrd.srpB()
            Objects.requireNonNull(srpB)
            val b = fromByteBuf(srpB!!)
            val bBytes = toBytesPadded(b)
            val u = fromByteBuf(sha256Digest(gABytes, bBytes))
            var bkgx = b.subtract(k.multiply(g.modPow(x, p)).mod(p))
            if (bkgx < BigInteger.ZERO) {
                bkgx = bkgx.add(p)
            }
            val s = bkgx.modPow(a.add(u.multiply(x)), p)
            val sBytes = toBytesPadded(s)
            val kBytes = sha256Digest(sBytes)

            // TODO: checks
            val m1 = sha256Digest(
                xor(sha256Digest(pBytes), sha256Digest(gBytes)),
                sha256Digest(salt1),
                sha256Digest(salt2),
                gABytes, bBytes, kBytes
            )
            val srpId: Long = pswrd.srpId()!!
            val icpsrp = ImmutableBaseInputCheckPasswordSRP.of(srpId, gABytes, m1)
            clientGroup.send<Authorization?, ImmutableCheckPassword>(DcId.main(), ImmutableCheckPassword.of(icpsrp))
        }
    }

    // > the numbers must be used in big-endian form, padded to 2048 bits
    private fun toBytesPadded(value: BigInteger): ByteBuf {
        val bytes = value.toByteArray()
        if (bytes.size > 256) {
            val correctedAuth = ByteArray(256)
            System.arraycopy(bytes, 1, correctedAuth, 0, 256)
            return Unpooled.wrappedBuffer(correctedAuth)
        } else if (bytes.size < 256) {
            val correctedAuth = ByteArray(256)
            System.arraycopy(bytes, 0, correctedAuth, 256 - bytes.size, bytes.size)
            for (a in 0 until 256 - bytes.size) {
                correctedAuth[a] = 0
            }
            return Unpooled.wrappedBuffer(correctedAuth)
        }
        return Unpooled.wrappedBuffer(bytes)
    }

    private fun random2048Number(): BigInteger {
        val b = ByteArray(2048 / 8)
        random.nextBytes(b)
        return fromByteArray(b)
    }

    private fun pbkdf2HmacSha512Iter100000(password: ByteBuf?, salt: ByteBuf?): ByteBuf {
        return try {
            val saltBytes = ByteBufUtil.getBytes(salt)
            val passwdBytes = ByteBufUtil.getBytes(password)
            val prf = Mac.getInstance("HmacSHA512")
            Unpooled.wrappedBuffer(deriveKey(prf, passwdBytes, saltBytes, 100000, 512))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    // copied from com.sun.crypto.provider.PBKDF2KeyImpl
    // because the public interface does not allow to work directly with byte arrays
    private fun deriveKey(prf: Mac, password: ByteArray, salt: ByteArray?, iterCount: Int, keyLengthInBit: Int): ByteArray {
        val keyLength = keyLengthInBit / 8
        val key = ByteArray(keyLength)
        try {
            val hlen = prf.macLength
            val intL = (keyLength + hlen - 1) / hlen // ceiling
            val intR = keyLength - (intL - 1) * hlen // residue
            val ui = ByteArray(hlen)
            val ti = ByteArray(hlen)
            // SecretKeySpec cannot be used, since password can be empty here.
            val macKey: SecretKey = object : SecretKey {
                override fun getAlgorithm(): String {
                    return prf.algorithm
                }

                override fun getFormat(): String {
                    return "RAW"
                }

                override fun getEncoded(): ByteArray {
                    return password.clone()
                }

                override fun hashCode(): Int {
                    return password.contentHashCode() * 41 +
                        prf.algorithm.lowercase().hashCode()
                }

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (this.javaClass != other!!.javaClass) return false
                    val sk = other as SecretKey?
                    return prf.algorithm.equals(sk!!.algorithm, ignoreCase = true) &&
                        MessageDigest.isEqual(password, sk.encoded)
                }
            }
            prf.init(macKey)
            val ibytes = ByteArray(4)
            for (i in 1..intL) {
                prf.update(salt)
                ibytes[3] = i.toByte()
                ibytes[2] = (i shr 8 and 0xff).toByte()
                ibytes[1] = (i shr 16 and 0xff).toByte()
                ibytes[0] = (i shr 24 and 0xff).toByte()
                prf.update(ibytes)
                prf.doFinal(ui, 0)
                System.arraycopy(ui, 0, ti, 0, ui.size)
                for (j in 2..iterCount) {
                    prf.update(ui)
                    prf.doFinal(ui, 0)
                    // XOR the intermediate Ui's together.
                    for (k in ui.indices) {
                        ti[k] = (ti[k].toInt() xor ui[k].toInt()).toByte()
                    }
                }
                if (i == intL) {
                    System.arraycopy(ti, 0, key, (i - 1) * hlen, intR)
                } else {
                    System.arraycopy(ti, 0, key, (i - 1) * hlen, hlen)
                }
            }
        } catch (gse: GeneralSecurityException) {
            throw RuntimeException("Error deriving PBKDF2 keys", gse)
        }
        return key
    }
}