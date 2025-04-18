package org.nocturne.logic

import net.dv8tion.jda.api.events.message.GenericMessageEvent
import org.nocturne.webserver.WebServer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object Util {
}

object AES {
    private const val ALGORITHM = "AES"

    fun encrypt(message: String, key: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(getKeyBytes(key), ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedBase64: String, key: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(getKeyBytes(key), ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getKeyBytes(key: String): ByteArray {
        // AES supports key sizes of 16, 24, or 32 bytes.
        // Here we ensure it's 16 bytes by padding or trimming
        val keyBytes = key.toByteArray(Charsets.UTF_8)
        return keyBytes.copyOf(16)
    }
}
