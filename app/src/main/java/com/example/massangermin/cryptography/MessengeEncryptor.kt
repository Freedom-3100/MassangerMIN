package com.example.massangermin.cryptography
import com.example.massangermin.cryptography.KeysGenerator
import java.security.KeyPair
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ECDHEncryptor(private val keysGenerator: KeysGenerator = KeysGenerator()) {

    data class EncryptionContext(
        val keyPair: KeyPair,
        var sharedSecret: ByteArray? = null
    ) {
        fun clear() {
            sharedSecret?.fill(0)
            sharedSecret = null

        }
    }

    companion object {
        private const val AES_KEY_SIZE = 256 // бит
        private const val GCM_TAG_LENGTH = 128 // бит
        private const val GCM_IV_LENGTH = 12 // байт
    }

    fun createContext(): EncryptionContext {
        return EncryptionContext(
            keyPair = keysGenerator.generate_key_pair()
        )
    }

    fun computeSharedSecret(context: EncryptionContext, otherPartyPublicKey: ByteArray) {
        val secret = keysGenerator.caclc_shared_secret(
            context.keyPair.private,
            otherPartyPublicKey
        )
        context.sharedSecret = secret
    }

    private fun deriveAESKey(sharedSecret: ByteArray): SecretKey {
        val keyBytes = sharedSecret.copyOf(32) // Берем первые 32 байта для AES-256

        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptMessage(context: EncryptionContext, plaintext: ByteArray): EncryptedMessage {
        require(context.sharedSecret != null) { "Shared secret must be computed first" }

        val aesKey = deriveAESKey(context.sharedSecret!!)

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec)

        val ciphertext = cipher.doFinal(plaintext)

        return EncryptedMessage(iv, ciphertext)
    }


    fun decryptMessage(context: EncryptionContext, encryptedMessage: EncryptedMessage): ByteArray {
        require(context.sharedSecret != null) { "Shared secret must be computed first" }

        val aesKey = deriveAESKey(context.sharedSecret!!)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, encryptedMessage.iv)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, parameterSpec)

        return cipher.doFinal(encryptedMessage.ciphertext)
    }

    data class EncryptedMessage(
        val iv: ByteArray,      // Initialization Vector (12 байт для GCM)
        val ciphertext: ByteArray // Зашифрованные данные
    ) {
        fun toByteArray(): ByteArray {
            return iv + ciphertext
        }

        companion object {
            fun fromByteArray(data: ByteArray): EncryptedMessage {
                require(data.size > GCM_IV_LENGTH) { "Invalid encrypted data" }
                val iv = data.copyOfRange(0, GCM_IV_LENGTH)
                val ciphertext = data.copyOfRange(GCM_IV_LENGTH, data.size)
                return EncryptedMessage(iv, ciphertext)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedMessage

            if (!iv.contentEquals(other.iv)) return false
            if (!ciphertext.contentEquals(other.ciphertext)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = iv.contentHashCode()
            result = 31 * result + ciphertext.contentHashCode()
            return result
        }
    }

    fun clearContext(context: EncryptionContext) {
        context.clear()
    }
}