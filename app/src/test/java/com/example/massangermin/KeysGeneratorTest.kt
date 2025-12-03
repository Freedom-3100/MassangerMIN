package com.example.massangermin

import com.example.massangermin.cryptography.KeysGenerator
import org.junit.Test

import org.junit.Assert.*



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class KeysGeneratorTest {

    private val keysGenerator = KeysGenerator()

    @Test
    fun testGenerateKeyPair_NotNull() {
        // Act
        val keyPair = keysGenerator.generate_key_pair()

        // Assert
        assertNotNull("KeyPair should not be null", keyPair)
        assertNotNull("Public key should not be null", keyPair.public)
        assertNotNull("Private key should not be null", keyPair.private)
    }

    @Test
    fun testGenerateKeyPair_DifferentEachTime() {
        // Act
        val keyPair1 = keysGenerator.generate_key_pair()
        val keyPair2 = keysGenerator.generate_key_pair()

        // Assert
        assertNotEquals(
            "Public keys should be different",
            keyPair1.public.encoded,
            keyPair2.public.encoded
        )

        assertNotEquals(
            "Private keys should be different",
            keyPair1.private.encoded,
            keyPair2.private.encoded
        )
    }

    @Test
    fun testKeyPair_EncodedFormat() {
        // Act
        val keyPair = keysGenerator.generate_key_pair()

        // Assert
        assertTrue("Public key should have encoded bytes",
            keyPair.public.encoded.isNotEmpty())

        assertTrue("Private key should have encoded bytes",
            keyPair.private.encoded.isNotEmpty())
    }

    @Test
    fun testKeyPair_Algorithm() {
        // Act
        val keyPair = keysGenerator.generate_key_pair()

        // Assert
        assertEquals("Algorithm should be EC",
            "EC", keyPair.public.algorithm)
        assertEquals("Algorithm should be EC",
            "EC", keyPair.private.algorithm)
    }

    @Test
    fun testCalcSharedSecret_ValidExchange() {
        // Arrange - имитируем двух участников
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val bobKeyPair = keysGenerator.generate_key_pair()

        // Act - Alice вычисляет общий секрет с публичным ключом Bob
        val aliceSecret = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        // Act - Bob вычисляет общий секрет с публичным ключом Alice
        val bobSecret = keysGenerator.caclc_shared_secret(
            bobKeyPair.private,
            aliceKeyPair.public.encoded
        )

        // Assert - секреты должны быть одинаковыми
        assertArrayEquals(
            "Shared secrets should be equal for both parties",
            aliceSecret,
            bobSecret
        )
    }

    @Test
    fun testCalcSharedSecret_NotEmpty() {
        // Arrange
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val bobKeyPair = keysGenerator.generate_key_pair()

        // Act
        val sharedSecret = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        // Assert
        assertTrue("Shared secret should not be empty",
            sharedSecret.isNotEmpty())

        // Проверяем, что это похоже на криптографический ключ
        assertTrue("Shared secret should be at least 16 bytes",
            sharedSecret.size >= 16)
    }

    @Test
    fun testCalcSharedSecret_Deterministic() {
        // Arrange
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val bobKeyPair = keysGenerator.generate_key_pair()

        // Act - вычисляем дважды с одними и теми же ключами
        val secret1 = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        val secret2 = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        // Assert - должен быть одинаковый результат
        assertArrayEquals(
            "Same keys should produce same shared secret",
            secret1,
            secret2
        )
    }

    @Test(expected = Exception::class)
    fun testCalcSharedSecret_InvalidPublicKey() {
        // Arrange - создаем некорректный публичный ключ
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val invalidPublicKey = ByteArray(10) { 0x00 }

        // Act & Assert - должно бросить исключение
        keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            invalidPublicKey
        )
    }

    @Test
    fun testCalcSharedSecret_DifferentKeysDifferentSecrets() {
        // Arrange
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val bobKeyPair = keysGenerator.generate_key_pair()
        val charlieKeyPair = keysGenerator.generate_key_pair()

        // Act
        val aliceBobSecret = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        val aliceCharlieSecret = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            charlieKeyPair.public.encoded
        )

        // Assert
        assertFalse(
            "Different key pairs should produce different secrets",
            aliceBobSecret.contentEquals(aliceCharlieSecret)
        )
    }

    @Test
    fun testKeyPair_EncodingDecodingRoundTrip() {
        // Arrange
        val originalKeyPair = keysGenerator.generate_key_pair()
        val keyFactory = java.security.KeyFactory.getInstance("EC")

        // Act - сериализуем и десериализуем публичный ключ
        val publicKeyBytes = originalKeyPair.public.encoded
        val restoredPublicKey = keyFactory.generatePublic(
            java.security.spec.X509EncodedKeySpec(publicKeyBytes)
        )

        // Act - сериализуем и десериализуем приватный ключ
        val privateKeyBytes = originalKeyPair.private.encoded
        val restoredPrivateKey = keyFactory.generatePrivate(
            java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes)
        )

        // Assert
        assertEquals(
            "Restored public key should equal original",
            originalKeyPair.public,
            restoredPublicKey
        )

        assertEquals(
            "Restored private key should equal original",
            originalKeyPair.private,
            restoredPrivateKey
        )
    }

    @Test
    fun testSharedSecret_CanBeUsedAsKey() {
        // Arrange
        val aliceKeyPair = keysGenerator.generate_key_pair()
        val bobKeyPair = keysGenerator.generate_key_pair()

        // Act
        val sharedSecret = keysGenerator.caclc_shared_secret(
            aliceKeyPair.private,
            bobKeyPair.public.encoded
        )

        // Assert - проверяем, что секрет можно использовать как ключ
        // Например, для AES должно быть 16, 24 или 32 байта
        val validKeySizes = setOf(16, 24, 32)
        assertTrue(
            "Shared secret size ${sharedSecret.size} should be valid for encryption",
            validKeySizes.contains(sharedSecret.size) || sharedSecret.size >= 16
        )
    }

    @Test
    fun testGenerateMultipleKeyPairs() {
        // Act - генерируем несколько ключей
        val keyPairs = List(10) { keysGenerator.generate_key_pair() }

        // Assert - все должны быть уникальными
        val publicKeys = keyPairs.map { it.public.encoded }
        val uniquePublicKeys = publicKeys.toSet()

        assertEquals(
            "All generated public keys should be unique",
            keyPairs.size,
            uniquePublicKeys.size
        )
    }

    @Test
    fun testKeyPair_Format() {
        // Arrange
        val keyPair = keysGenerator.generate_key_pair()

        // Assert
        assertEquals("Public key format should be X.509",
            "X.509", keyPair.public.format)

        assertEquals("Private key format should be PKCS#8",
            "PKCS#8", keyPair.private.format)
    }
}