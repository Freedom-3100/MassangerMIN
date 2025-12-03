package com.example.massangermin

import com.example.massangermin.cryptography.ECDHEncryptor
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.nio.charset.StandardCharsets
import java.util.Random


class ECDHEncryptorTest {

    private lateinit var encryptor: ECDHEncryptor
    private lateinit var aliceContext: ECDHEncryptor.EncryptionContext
    private lateinit var bobContext: ECDHEncryptor.EncryptionContext

    @Before
    fun setUp() {
        encryptor = ECDHEncryptor()
        aliceContext = encryptor.createContext()
        bobContext = encryptor.createContext()
    }

    @Test
    fun `test createContext creates valid context`() {
        // Arrange & Act
        val context = encryptor.createContext()

        // Assert
        assertNotNull(context)
        assertNotNull(context.keyPair)
        assertNotNull(context.keyPair.public)
        assertNotNull(context.keyPair.private)
        assertNull(context.sharedSecret) // Shared secret should be null initially
    }

    @Test
    fun `test computeSharedSecret sets shared secret`() {
        // Arrange
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        // Act
        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Assert
        assertNotNull(aliceContext.sharedSecret)
        assertNotNull(bobContext.sharedSecret)
        assertTrue(aliceContext.sharedSecret!!.isNotEmpty())
        assertTrue(bobContext.sharedSecret!!.isNotEmpty())
    }

    @Test
    fun `test computeSharedSecret produces same secret for both parties`() {
        // Arrange
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        // Act
        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Assert
        val aliceSecret = aliceContext.sharedSecret!!
        val bobSecret = bobContext.sharedSecret!!

        assertArrayEquals(
            "Shared secrets should be identical for both parties",
            aliceSecret,
            bobSecret
        )
    }

    @Test
    fun `test encrypt and decrypt message between two parties`() {
        // Arrange
        val message = "Hello Bob! This is Alice.".toByteArray()

        // Обмен публичными ключами
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        // Вычисление общего секрета
        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act - Alice шифрует сообщение
        val encryptedMessage = encryptor.encryptMessage(aliceContext, message)

        // Act - Bob дешифрует сообщение
        val decryptedMessage = encryptor.decryptMessage(bobContext, encryptedMessage)

        // Assert
        assertArrayEquals(
            "Decrypted message should match original",
            message,
            decryptedMessage
        )
    }

    @Test
    fun `test encrypt and decrypt multiple messages`() {
        // Arrange
        val messages = listOf(
            "Message 1",
            "Another secret message",
            "Тест на русском языке",
            "🎉 Emoji test 🚀"
        ).map { it.toByteArray() }

        // Обмен публичными ключами
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act & Assert для каждого сообщения
        messages.forEach { originalMessage ->
            val encrypted = encryptor.encryptMessage(aliceContext, originalMessage)
            val decrypted = encryptor.decryptMessage(bobContext, encrypted)

            assertArrayEquals(
                "Message should be correctly encrypted and decrypted: ${String(originalMessage)}",
                originalMessage,
                decrypted
            )
        }
    }

    @Test
    fun `test encrypted messages are different each time`() {
        // Arrange
        val message = "Same message, different encryption".toByteArray()
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act - шифруем одно и то же сообщение дважды
        val encrypted1 = encryptor.encryptMessage(aliceContext, message)
        val encrypted2 = encryptor.encryptMessage(aliceContext, message)

        // Assert - IV должны быть разными, поэтому шифротексты разные
        assertFalse(
            "IVs should be different for each encryption",
            encrypted1.iv.contentEquals(encrypted2.iv)
        )

        assertFalse(
            "Ciphertexts should be different for each encryption",
            encrypted1.ciphertext.contentEquals(encrypted2.ciphertext)
        )

        // Но оба должны корректно дешифровываться
        val decrypted1 = encryptor.decryptMessage(bobContext, encrypted1)
        val decrypted2 = encryptor.decryptMessage(bobContext, encrypted2)

        assertArrayEquals(message, decrypted1)
        assertArrayEquals(message, decrypted2)
    }

    @Test
    fun `test EncryptedMessage serialization and deserialization`() {
        // Arrange
        val originalMessage = ECDHEncryptor.EncryptedMessage(
            iv = ByteArray(12).apply { Random().nextBytes(this) },
            ciphertext = "Secret ciphertext".toByteArray()
        )

        // Act
        val serialized = originalMessage.toByteArray()
        val deserialized = ECDHEncryptor.EncryptedMessage.fromByteArray(serialized)

        // Assert
        assertArrayEquals(
            "IV should be preserved after serialization",
            originalMessage.iv,
            deserialized.iv
        )

        assertArrayEquals(
            "Ciphertext should be preserved after serialization",
            originalMessage.ciphertext,
            deserialized.ciphertext
        )

        assertEquals(
            "Objects should be equal",
            originalMessage,
            deserialized
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test EncryptedMessage fromByteArray with invalid data throws exception`() {
        // Arrange
        val invalidData = ByteArray(5) // Меньше, чем GCM_IV_LENGTH

        // Act & Assert
        ECDHEncryptor.EncryptedMessage.fromByteArray(invalidData)
    }

    @Test
    fun `test clearContext removes shared secret`() {
        // Arrange
        val alicePublicKey = aliceContext.keyPair.public.encoded
        encryptor.computeSharedSecret(bobContext, alicePublicKey)
        assertNotNull(bobContext.sharedSecret)

        // Act
        encryptor.clearContext(bobContext)

        // Assert
        assertNull(bobContext.sharedSecret)
    }

    @Test
    fun `test different key pairs produce different shared secrets simple`() {
        // Arrange
        val charlieContext = encryptor.createContext()

        // Act
        // Alice + Bob
        encryptor.computeSharedSecret(aliceContext, bobContext.keyPair.public.encoded)
        val aliceBobSecret = aliceContext.sharedSecret!!.copyOf()

        // Очищаем Alice и вычисляем с Charlie
        aliceContext.sharedSecret = null
        encryptor.computeSharedSecret(aliceContext, charlieContext.keyPair.public.encoded)
        val aliceCharlieSecret = aliceContext.sharedSecret!!.copyOf()

        // Также Bob + Charlie (используем новый контекст для Bob)
        val bobContext2 = encryptor.createContext()
        encryptor.computeSharedSecret(bobContext2, charlieContext.keyPair.public.encoded)
        val bobCharlieSecret = bobContext2.sharedSecret!!

        // Assert
        assertFalse(
            "Alice-Bob secret should differ from Alice-Charlie secret",
            aliceBobSecret.contentEquals(aliceCharlieSecret)
        )

        assertFalse(
            "Alice-Bob secret should differ from Bob-Charlie secret",
            aliceBobSecret.contentEquals(bobCharlieSecret)
        )

        assertFalse(
            "Alice-Charlie secret should differ from Bob-Charlie secret",
            aliceCharlieSecret.contentEquals(bobCharlieSecret)
        )

        // Очистка
        encryptor.clearContext(charlieContext)
        encryptor.clearContext(bobContext2)
    }

    @Test
    fun `test encryption fails without shared secret`() {
        // Arrange
        val message = "Test message".toByteArray()

        // Act & Assert
        try {
            encryptor.encryptMessage(aliceContext, message)
            fail("Should have thrown exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Shared secret must be computed first", e.message)
        }
    }

    @Test
    fun `test decryption fails without shared secret`() {
        // Arrange
        val encryptedMessage = ECDHEncryptor.EncryptedMessage(
            ByteArray(12),
            ByteArray(10)
        )

        // Act & Assert
        try {
            encryptor.decryptMessage(aliceContext, encryptedMessage)
            fail("Should have thrown exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Shared secret must be computed first", e.message)
        }
    }

    @Test
    fun `test two way communication`() {
        // Arrange
        val aliceMessage = "Hello from Alice!".toByteArray()
        val bobMessage = "Hello from Bob!".toByteArray()

        // Обмен ключами
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act - Alice отправляет сообщение Bobу
        val aliceToBobEncrypted = encryptor.encryptMessage(aliceContext, aliceMessage)
        val aliceToBobDecrypted = encryptor.decryptMessage(bobContext, aliceToBobEncrypted)

        // Act - Bob отвечает Alice
        val bobToAliceEncrypted = encryptor.encryptMessage(bobContext, bobMessage)
        val bobToAliceDecrypted = encryptor.decryptMessage(aliceContext, bobToAliceEncrypted)

        // Assert
        assertArrayEquals("Alice's message should be correctly received by Bob",
            aliceMessage, aliceToBobDecrypted)
        assertArrayEquals("Bob's message should be correctly received by Alice",
            bobMessage, bobToAliceDecrypted)
    }

    @Test
    fun `test with large message`() {
        // Arrange
        val largeMessage = ByteArray(1024 * 10) // 10KB
        Random().nextBytes(largeMessage)

        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act
        val encrypted = encryptor.encryptMessage(aliceContext, largeMessage)
        val decrypted = encryptor.decryptMessage(bobContext, encrypted)

        // Assert
        assertArrayEquals(
            "Large message should be correctly encrypted and decrypted",
            largeMessage,
            decrypted
        )
    }

    @Test
    fun `test with empty message`() {
        // Arrange
        val emptyMessage = ByteArray(0)
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Act
        val encrypted = encryptor.encryptMessage(aliceContext, emptyMessage)
        val decrypted = encryptor.decryptMessage(bobContext, encrypted)

        // Assert
        assertArrayEquals(
            "Empty message should be correctly handled",
            emptyMessage,
            decrypted
        )
    }

    @Test
    fun `test EncryptedMessage equals and hashCode`() {
        // Arrange
        val iv = ByteArray(12).apply { Random().nextBytes(this) }
        val ciphertext = "Test data".toByteArray()

        val message1 = ECDHEncryptor.EncryptedMessage(iv, ciphertext)
        val message2 = ECDHEncryptor.EncryptedMessage(iv.copyOf(), ciphertext.copyOf())
        val message3 = ECDHEncryptor.EncryptedMessage(
            ByteArray(12).apply { Random().nextBytes(this) },
            ciphertext
        )

        // Assert
        assertEquals("Messages with same data should be equal", message1, message2)
        assertEquals("HashCodes should be equal for equal objects",
            message1.hashCode(), message2.hashCode())

        assertNotEquals("Messages with different IVs should not be equal",
            message1, message3)
        assertNotEquals("HashCodes should differ for different objects",
            message1.hashCode(), message3.hashCode())
    }

    @Test
    fun `test full ECDH workflow simulation`() {
        // Arrange - имитация двух независимых клиентов
        val aliceEncryptor = ECDHEncryptor()
        val bobEncryptor = ECDHEncryptor()

        val aliceContext = aliceEncryptor.createContext()
        val bobContext = bobEncryptor.createContext()

        // Шаг 1: Обмен публичными ключами (симуляция сети)
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        // Шаг 2: Каждая сторона вычисляет общий секрет
        aliceEncryptor.computeSharedSecret(aliceContext, bobPublicKey)
        bobEncryptor.computeSharedSecret(bobContext, alicePublicKey)

        // Шаг 3: Alice шифрует сообщение
        val message = "Confidential data".toByteArray()
        val encrypted = aliceEncryptor.encryptMessage(aliceContext, message)

        // Шаг 4: Bob дешифрует сообщение
        val decrypted = bobEncryptor.decryptMessage(bobContext, encrypted)

        // Assert
        assertArrayEquals(
            "Full ECDH workflow should work correctly",
            message,
            decrypted
        )

        // Очистка
        aliceEncryptor.clearContext(aliceContext)
        bobEncryptor.clearContext(bobContext)
    }

    @Test
    fun `test shared secret is 32 bytes for AES-256`() {
        // Arrange
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        // Act
        encryptor.computeSharedSecret(aliceContext, bobPublicKey)

        // Assert
        assertEquals(
            "Shared secret should be 32 bytes for AES-256",
            32,
            aliceContext.sharedSecret!!.size
        )
    }

    @Test
    fun `test message integrity - tampered ciphertext should fail`() {
        // Arrange
        val message = "Original message".toByteArray()
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        val encrypted = encryptor.encryptMessage(aliceContext, message)

        // Act & Assert - подмена ciphertext
        val tamperedCiphertext = encrypted.ciphertext.copyOf().apply {
            this[0] = (this[0] + 1).toByte() // Меняем один байт
        }
        val tamperedMessage = ECDHEncryptor.EncryptedMessage(
            encrypted.iv,
            tamperedCiphertext
        )

        try {
            encryptor.decryptMessage(bobContext, tamperedMessage)
            fail("Should have thrown exception for tampered ciphertext")
        } catch (e: Exception) {
            // GCM должен обнаружить подмену
            assertTrue(e is javax.crypto.AEADBadTagException || e is javax.crypto.BadPaddingException)
        }
    }

    @Test
    fun `test message integrity - tampered IV should fail`() {
        // Arrange
        val message = "Another message".toByteArray()
        val alicePublicKey = aliceContext.keyPair.public.encoded
        val bobPublicKey = bobContext.keyPair.public.encoded

        encryptor.computeSharedSecret(aliceContext, bobPublicKey)
        encryptor.computeSharedSecret(bobContext, alicePublicKey)

        val encrypted = encryptor.encryptMessage(aliceContext, message)

        // Act & Assert - подмена IV
        val tamperedIV = encrypted.iv.copyOf().apply {
            this[0] = (this[0] + 1).toByte()
        }
        val tamperedMessage = ECDHEncryptor.EncryptedMessage(
            tamperedIV,
            encrypted.ciphertext
        )

        try {
            encryptor.decryptMessage(bobContext, tamperedMessage)
            fail("Should have thrown exception for tampered IV")
        } catch (e: Exception) {
            // GCM должен обнаружить подмену
            assertTrue(e is javax.crypto.AEADBadTagException || e is javax.crypto.BadPaddingException)
        }
    }
}

class ECDHEncryptorIntegrationTest {

    @Test
    fun `test real world chat simulation`() {
        // Имитация реального чата между Alice и Bob

        // 1. Инициализация
        val aliceEncryptor = ECDHEncryptor()
        val bobEncryptor = ECDHEncryptor()

        val alice = aliceEncryptor.createContext()
        val bob = bobEncryptor.createContext()

        // 2. Обмен публичными ключами (например, через сервер)
        println("Exchanging public keys...")
        val alicePublicKey = alice.keyPair.public.encoded
        val bobPublicKey = bob.keyPair.public.encoded

        // 3. Установка общего секрета
        println("Computing shared secrets...")
        aliceEncryptor.computeSharedSecret(alice, bobPublicKey)
        bobEncryptor.computeSharedSecret(bob, alicePublicKey)

        // 4. Обмен сообщениями
        val conversation = listOf(
            "Alice" to "Hi Bob! How are you?",
            "Bob" to "Hi Alice! I'm good, thanks!",
            "Alice" to "Did you get the documents?",
            "Bob" to "Yes, they're encrypted and safe."
        )

        conversation.forEach { (sender, text) ->
            val encrypted = when (sender) {
                "Alice" -> aliceEncryptor.encryptMessage(alice, text.toByteArray())
                "Bob" -> bobEncryptor.encryptMessage(bob, text.toByteArray())
                else -> throw IllegalStateException()
            }

            val decrypted = when (sender) {
                "Alice" -> bobEncryptor.decryptMessage(bob, encrypted)
                "Bob" -> aliceEncryptor.decryptMessage(alice, encrypted)
                else -> throw IllegalStateException()
            }

            assertEquals(text, String(decrypted, StandardCharsets.UTF_8))
            println("$sender: $text ✓")
        }

        // 5. Очистка
        aliceEncryptor.clearContext(alice)
        bobEncryptor.clearContext(bob)

        println("Chat completed successfully!")
    }
}